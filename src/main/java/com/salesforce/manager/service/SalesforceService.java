package com.salesforce.manager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.salesforce.manager.config.SalesforceConfig;
import com.salesforce.manager.model.AuthResponse;
import com.salesforce.manager.model.DeployRequest;
import com.salesforce.manager.model.ValidationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceService {

    private final SalesforceConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(config.getAuthUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("scope", "api refresh_token")
                .build()
                .toUriString();
    }

    public AuthResponse getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("redirect_uri", config.getRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                config.getTokenUrl(), request, AuthResponse.class);

        return response.getBody();
    }

    public List<ValidationRule> getValidationRules(String accessToken, String instanceUrl) {
        String query = "SELECT Id, ValidationName, Active, EntityDefinition.DeveloperName, " +
                "ErrorMessage, Description, EntityDefinitionId, NamespacePrefix " +
                "FROM ValidationRule ORDER BY EntityDefinition.DeveloperName, ValidationName";

        String encodedQuery = java.net.URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = instanceUrl + "/services/data/" + config.getApiVersion() +
                "/tooling/query/?q=" + encodedQuery;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, request, JsonNode.class);

        List<ValidationRule> rules = new ArrayList<>();
        JsonNode records = response.getBody().get("records");

        if (records != null && records.isArray()) {
            for (JsonNode record : records) {
                ValidationRule rule = new ValidationRule();
                rule.setId(record.get("Id").asText());
                rule.setValidationName(getTextOrDefault(record, "ValidationName", "N/A"));
                rule.setActive(record.get("Active").asBoolean());
                rule.setErrorMessage(getTextOrDefault(record, "ErrorMessage", ""));
                rule.setDescription(getTextOrDefault(record, "Description", ""));

                JsonNode entityDef = record.get("EntityDefinition");
                String objName = (entityDef != null)
                    ? getTextOrDefault(entityDef, "DeveloperName", "Unknown")
                    : "Unknown";
                rule.setObjectName(objName);
                rule.setEntityDefinitionId(getTextOrDefault(record, "EntityDefinitionId", ""));

                String ns = getTextOrDefault(record, "NamespacePrefix", null);
                String vrName = rule.getValidationName();
                if (ns != null && !ns.isEmpty()) {
                    rule.setFullName(ns + "__" + objName + "." + vrName);
                } else {
                    rule.setFullName(objName + "." + vrName);
                }

                rules.add(rule);
            }
        }

        return rules;
    }

    public boolean toggleValidationRule(String accessToken, String instanceUrl,
                                        String ruleId, boolean active) {
        try {
            String getUrl = instanceUrl + "/services/data/" + config.getApiVersion() +
                    "/tooling/sobjects/ValidationRule/" + ruleId;

            HttpHeaders getHeaders = new HttpHeaders();
            getHeaders.setBearerAuth(accessToken);
            getHeaders.set("Accept", "application/json");

            ResponseEntity<JsonNode> getResponse = restTemplate.exchange(
                    getUrl, HttpMethod.GET, new HttpEntity<>(getHeaders), JsonNode.class);

            JsonNode metadata = getResponse.getBody().get("Metadata");
            if (metadata == null || metadata.isNull()) {
                log.error("No metadata found for rule {}", ruleId);
                return false;
            }

            ((ObjectNode) metadata).put("active", active);

            String patchUrl = instanceUrl + "/services/data/" + config.getApiVersion() +
                    "/tooling/sobjects/ValidationRule/" + ruleId;

            HttpHeaders patchHeaders = new HttpHeaders();
            patchHeaders.setBearerAuth(accessToken);
            patchHeaders.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode body = objectMapper.createObjectNode();
            body.set("Metadata", metadata);

            HttpEntity<ObjectNode> patchRequest = new HttpEntity<>(body, patchHeaders);

            ResponseEntity<String> patchResponse = restTemplate.exchange(
                    patchUrl, HttpMethod.PATCH, patchRequest, String.class);

            return patchResponse.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Error toggling validation rule {}", ruleId, e);
            return false;
        }
    }

    public List<String> deployChanges(String accessToken, String instanceUrl,
                                      DeployRequest request) {
        List<String> errors = new ArrayList<>();

        for (DeployRequest.RuleUpdate rule : request.getRules()) {
            boolean success = toggleValidationRule(
                    accessToken, instanceUrl, rule.getId(), rule.isActive());
            if (!success) {
                errors.add(rule.getFullName());
            }
        }

        return errors;
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText() : defaultValue;
    }
}

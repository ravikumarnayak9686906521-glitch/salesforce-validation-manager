package com.salesforce.vm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.salesforce.vm.config.SalesforceConfig;
import com.salesforce.vm.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationRuleService {

    private final SalesforceConfig config;
    private final TokenStore tokenStore;
    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.builder().build();

    public List<ValidationRule> getValidationRules(String sessionId) {
        SalesforceAuthResponse auth = getAuth(sessionId);
        String query = "SELECT Id, ValidationName, Active, Description, ErrorMessage, " +
                "EntityDefinition.DeveloperName, EntityDefinition.QualifiedApiName " +
                "FROM ValidationRule WHERE EntityDefinition.DeveloperName = 'Account'";

        String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
        String url = auth.getInstanceUrl() + "/services/data/" + config.getApiVersion() + 
                     "/tooling/query?q=" + encodedQuery;

        try {
            ToolingQueryResponse<ValidationRule> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ToolingQueryResponse<ValidationRule>>() {})
                    .block();

            return response != null && response.getRecords() != null ? response.getRecords() : List.of();
        } catch (WebClientResponseException e) {
            log.error("Error fetching validation rules: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch validation rules: " + e.getMessage());
        }
    }

    public ValidationRule toggleValidationRule(String sessionId, String ruleId, boolean active) {
        SalesforceAuthResponse auth = getAuth(sessionId);
        String url = auth.getInstanceUrl() + "/services/data/" + config.getApiVersion() + 
                     "/tooling/sobjects/ValidationRule/" + ruleId;

        ObjectNode body = objectMapper.createObjectNode();
        body.put("Active", active);

        try {
            webClient.patch()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully toggled validation rule {} to active={}", ruleId, active);

            // Return updated rule info
            ValidationRule rule = new ValidationRule();
            rule.setId(ruleId);
            rule.setActive(active);
            return rule;
        } catch (WebClientResponseException e) {
            log.error("Error toggling validation rule: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to toggle validation rule: " + e.getMessage());
        }
    }

    public String deployChanges(String sessionId, List<ValidationRuleUpdateRequest> changes) {
        SalesforceAuthResponse auth = getAuth(sessionId);
        int successCount = 0;
        int failCount = 0;

        for (ValidationRuleUpdateRequest change : changes) {
            try {
                toggleValidationRule(sessionId, change.getId(), change.isActive());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to deploy change for rule {}: {}", change.getId(), e.getMessage());
                failCount++;
            }
        }

        return String.format("Deployment complete. Success: %d, Failed: %d", successCount, failCount);
    }

    private SalesforceAuthResponse getAuth(String sessionId) {
        SalesforceAuthResponse auth = tokenStore.getToken(sessionId);
        if (auth == null) {
            throw new RuntimeException("Not authenticated with Salesforce. Please login first.");
        }
        return auth;
    }
}

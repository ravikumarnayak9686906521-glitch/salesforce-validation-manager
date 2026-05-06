package com.salesforce.vm.service;

import com.salesforce.vm.config.SalesforceConfig;
import com.salesforce.vm.model.SalesforceAuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceAuthService {

    private final SalesforceConfig config;
    private final TokenStore tokenStore;
    private final WebClient webClient = WebClient.builder().build();

    public String buildAuthorizationUrl() {
        String state = java.util.UUID.randomUUID().toString();
        return config.getLoginUrl() + "/services/oauth2/authorize" +
                "?response_type=code" +
                "&client_id=" + URLEncoder.encode(config.getClientId(), StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode("api refresh_token", StandardCharsets.UTF_8) +
                "&state=" + state +
                "&prompt=login";
    }

    public SalesforceAuthResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("client_id", config.getClientId());
        formData.add("client_secret", config.getClientSecret());
        formData.add("redirect_uri", config.getRedirectUri());

        try {
            SalesforceAuthResponse response = webClient.post()
                    .uri(config.getLoginUrl() + "/services/oauth2/token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(SalesforceAuthResponse.class)
                    .block();

            log.info("Successfully obtained access token for instance: {}", 
                    response != null ? response.getInstanceUrl() : "unknown");
            return response;
        } catch (Exception e) {
            log.error("Error exchanging code for token: {}", e.getMessage());
            throw new RuntimeException("Failed to authenticate with Salesforce: " + e.getMessage());
        }
    }

    public String initiateAuth() {
        return buildAuthorizationUrl();
    }

    public String handleCallback(String code) {
        SalesforceAuthResponse authResponse = exchangeCodeForToken(code);
        return tokenStore.saveToken(authResponse);
    }

    public void logout(String sessionId) {
        tokenStore.removeToken(sessionId);
    }

    public boolean isAuthenticated(String sessionId) {
        return tokenStore.isValidSession(sessionId);
    }

    public SalesforceAuthResponse getAuthResponse(String sessionId) {
        return tokenStore.getToken(sessionId);
    }
}

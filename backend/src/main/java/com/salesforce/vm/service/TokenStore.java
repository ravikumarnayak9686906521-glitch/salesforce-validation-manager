package com.salesforce.vm.service;

import com.salesforce.vm.model.SalesforceAuthResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final Map<String, SalesforceAuthResponse> store = new ConcurrentHashMap<>();

    public String saveToken(SalesforceAuthResponse authResponse) {
        String sessionId = UUID.randomUUID().toString();
        store.put(sessionId, authResponse);
        return sessionId;
    }

    public SalesforceAuthResponse getToken(String sessionId) {
        return store.get(sessionId);
    }

    public void removeToken(String sessionId) {
        store.remove(sessionId);
    }

    public boolean isValidSession(String sessionId) {
        return sessionId != null && store.containsKey(sessionId);
    }
}

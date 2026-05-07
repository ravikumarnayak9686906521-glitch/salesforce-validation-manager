package com.salesforce.manager.model;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String instanceUrl;
    private String id;
    private String tokenType;
    private String issuedAt;
    private String signature;
}

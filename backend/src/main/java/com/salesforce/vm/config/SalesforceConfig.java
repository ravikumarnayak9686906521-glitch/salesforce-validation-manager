package com.salesforce.vm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "salesforce")
public class SalesforceConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String loginUrl;
    private String apiVersion;
}

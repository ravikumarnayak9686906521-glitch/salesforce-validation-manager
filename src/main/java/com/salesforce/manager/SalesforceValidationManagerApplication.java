package com.salesforce.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.salesforce.manager.config.SalesforceConfig;

@SpringBootApplication
@EnableConfigurationProperties(SalesforceConfig.class)
public class SalesforceValidationManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalesforceValidationManagerApplication.class, args);
    }
}

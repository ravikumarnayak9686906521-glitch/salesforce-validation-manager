package com.salesforce.manager.model;

import lombok.Data;

@Data
public class ValidationRule {
    private String id;
    private String validationName;
    private boolean active;
    private String objectName;
    private String errorMessage;
    private String description;
    private String entityDefinitionId;
    private String fullName;
}

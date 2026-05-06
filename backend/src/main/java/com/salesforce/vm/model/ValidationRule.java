package com.salesforce.vm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRule {
    private String id;

    @JsonProperty("ValidationName")
    private String validationName;

    @JsonProperty("Active")
    private Boolean active;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @JsonProperty("EntityDefinition")
    private EntityDefinition entityDefinition;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityDefinition {
        @JsonProperty("DeveloperName")
        private String developerName;

        @JsonProperty("QualifiedApiName")
        private String qualifiedApiName;
    }

    // Helper method
    public String getEntityName() {
        return entityDefinition != null ? entityDefinition.getDeveloperName() : "Account";
    }
}

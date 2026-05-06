package com.salesforce.vm.model;

import lombok.Data;

@Data
public class ValidationRuleUpdateRequest {
    private String id;
    private boolean active;
}

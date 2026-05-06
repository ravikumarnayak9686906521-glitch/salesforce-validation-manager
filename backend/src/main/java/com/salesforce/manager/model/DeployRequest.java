package com.salesforce.manager.model;

import lombok.Data;
import java.util.List;

@Data
public class DeployRequest {
    private List<RuleUpdate> rules;

    @Data
    public static class RuleUpdate {
        private String id;
        private String fullName;
        private boolean active;
    }
}

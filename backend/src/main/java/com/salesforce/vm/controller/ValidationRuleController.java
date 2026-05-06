package com.salesforce.vm.controller;

import com.salesforce.vm.model.ApiResponse;
import com.salesforce.vm.model.ValidationRule;
import com.salesforce.vm.model.ValidationRuleUpdateRequest;
import com.salesforce.vm.service.ValidationRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/validation-rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowCredentials = "true")
public class ValidationRuleController {

    private final ValidationRuleService validationRuleService;

    @GetMapping
    public ApiResponse<List<ValidationRule>> getValidationRules(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        try {
            if (sessionId == null) {
                return ApiResponse.error("No session ID provided. Please login first.");
            }
            List<ValidationRule> rules = validationRuleService.getValidationRules(sessionId);
            return ApiResponse.success("Validation rules fetched successfully", rules);
        } catch (Exception e) {
            log.error("Error getting validation rules: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @PatchMapping("/{id}/toggle")
    public ApiResponse<ValidationRule> toggleValidationRule(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable("id") String ruleId,
            @RequestBody Map<String, Boolean> body) {
        try {
            if (sessionId == null) {
                return ApiResponse.error("No session ID provided. Please login first.");
            }
            boolean active = body.getOrDefault("active", false);
            ValidationRule rule = validationRuleService.toggleValidationRule(sessionId, ruleId, active);
            return ApiResponse.success(
                "Validation rule " + (active ? "activated" : "deactivated") + " successfully", rule);
        } catch (Exception e) {
            log.error("Error toggling validation rule: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/deploy")
    public ApiResponse<String> deployChanges(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestBody List<ValidationRuleUpdateRequest> changes) {
        try {
            if (sessionId == null) {
                return ApiResponse.error("No session ID provided. Please login first.");
            }
            String result = validationRuleService.deployChanges(sessionId, changes);
            return ApiResponse.success("Deployment completed", result);
        } catch (Exception e) {
            log.error("Error deploying changes: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/toggle-all")
    public ApiResponse<String> toggleAll(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestBody Map<String, Boolean> body) {
        try {
            if (sessionId == null) {
                return ApiResponse.error("No session ID provided. Please login first.");
            }
            boolean active = body.getOrDefault("active", false);
            List<ValidationRule> rules = validationRuleService.getValidationRules(sessionId);

            int count = 0;
            for (ValidationRule rule : rules) {
                if (rule.getActive() != active) {
                    validationRuleService.toggleValidationRule(sessionId, rule.getId(), active);
                    count++;
                }
            }
            return ApiResponse.success(
                String.format("%d validation rules %s successfully", count, active ? "activated" : "deactivated"));
        } catch (Exception e) {
            log.error("Error toggling all rules: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}

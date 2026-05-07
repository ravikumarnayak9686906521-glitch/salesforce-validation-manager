package com.salesforce.manager.controller;

import com.salesforce.manager.model.DeployRequest;
import com.salesforce.manager.model.ValidationRule;
import com.salesforce.manager.service.SalesforceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/validation-rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ValidationRuleController {

    private final SalesforceService salesforceService;

    @GetMapping
    public ResponseEntity<List<ValidationRule>> getRules(
            @RequestHeader("X-Access-Token") String accessToken,
            @RequestHeader("X-Instance-Url") String instanceUrl) {
        List<ValidationRule> rules = salesforceService.getValidationRules(accessToken, instanceUrl);
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/deploy")
    public ResponseEntity<Map<String, Object>> deploy(
            @RequestHeader("X-Access-Token") String accessToken,
            @RequestHeader("X-Instance-Url") String instanceUrl,
            @RequestBody DeployRequest request) {

        List<String> errors = salesforceService.deployChanges(accessToken, instanceUrl, request);

        return ResponseEntity.ok(Map.of(
                "success", errors.isEmpty(),
                "failedRules", errors
        ));
    }
}

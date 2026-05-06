package com.salesforce.manager.controller;

import com.salesforce.manager.model.AuthResponse;
import com.salesforce.manager.service.SalesforceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final SalesforceService salesforceService;

    @GetMapping("/url")
    public ResponseEntity<Map<String, String>> getAuthUrl() {
        String url = salesforceService.getAuthorizationUrl();
        return ResponseEntity.ok(Map.of("authUrl", url));
    }

    @PostMapping("/token")
    public ResponseEntity<AuthResponse> getToken(@RequestParam String code) {
        AuthResponse response = salesforceService.getAccessToken(code);
        return ResponseEntity.ok(response);
    }
}

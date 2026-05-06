package com.salesforce.vm.controller;

import com.salesforce.vm.model.ApiResponse;
import com.salesforce.vm.service.SalesforceAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/salesforce")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowCredentials = "true")
public class AuthController {

    private final SalesforceAuthService authService;

    @Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @GetMapping("/url")
    public ApiResponse<Map<String, String>> getAuthUrl() {
        String authUrl = authService.initiateAuth();
        return ApiResponse.success(Map.of("authUrl", authUrl));
    }

    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code, 
                         @RequestParam(value = "state", required = false) String state,
                         HttpServletResponse response) throws IOException {
        try {
            String sessionId = authService.handleCallback(code);
            log.info("Salesforce OAuth successful, redirecting to frontend with session");
            response.sendRedirect(frontendUrl + "/dashboard?session=" + sessionId);
        } catch (Exception e) {
            log.error("OAuth callback error: {}", e.getMessage());
            response.sendRedirect(frontendUrl + "/login?error=" + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Boolean>> checkAuth(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        boolean isAuth = sessionId != null && authService.isAuthenticated(sessionId);
        return ApiResponse.success(Map.of("authenticated", isAuth));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (sessionId != null) {
            authService.logout(sessionId);
        }
        return ApiResponse.success("Logged out successfully");
    }
}

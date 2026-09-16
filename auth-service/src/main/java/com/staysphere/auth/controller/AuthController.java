package com.staysphere.auth.controller;

import com.staysphere.auth.dto.AuthResponse;
import com.staysphere.auth.dto.LoginRequest;
import com.staysphere.auth.dto.RefreshTokenRequest;
import com.staysphere.auth.dto.RegisterRequest;
import com.staysphere.auth.service.AuthService;
import com.staysphere.auth.service.TokenDenylistService;
import com.staysphere.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenDenylistService tokenDenylistService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Registration successful",
                        authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Login successful",
                        authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed",
                        authService.refreshToken(
                                request.getRefreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenDenylistService.deny(authorization.substring(7), 900000);
        }
        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Auth service is running"));
    }
}
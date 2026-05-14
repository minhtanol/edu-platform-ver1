package com.edu.app.auth;

import com.edu.app.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor
public class AuthController {
  private final AuthService service;
  @PostMapping("/login") public ApiResponse<AuthDtos.TokenResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) { return ApiResponse.ok(service.login(req)); }
  @PostMapping("/refresh") public ApiResponse<AuthDtos.TokenResponse> refresh(@Valid @RequestBody AuthDtos.RefreshRequest req) { return ApiResponse.ok(service.refresh(req.refreshToken())); }
  @PostMapping("/logout") public ApiResponse<Void> logout(@Valid @RequestBody AuthDtos.RefreshRequest req) { service.logout(req.refreshToken()); return ApiResponse.ok(null); }
  @PostMapping("/forgot-password") public ApiResponse<Void> forgot(@Valid @RequestBody AuthDtos.ForgotPasswordRequest req) { service.forgot(req.email()); return ApiResponse.ok(null); }
  @PostMapping("/reset-password") public ApiResponse<Void> reset(@Valid @RequestBody AuthDtos.ResetPasswordRequest req) { service.reset(req); return ApiResponse.ok(null); }
}

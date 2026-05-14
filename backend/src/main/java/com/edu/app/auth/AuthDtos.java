package com.edu.app.auth;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.util.Set;

public class AuthDtos {
  public record LoginRequest(@Email String email, @NotBlank String password) {}
  public record TokenResponse(String accessToken, String refreshToken, UUID id, String email, String fullName, Set<String> roles) {}
  public record RefreshRequest(@NotBlank String refreshToken) {}
  public record ForgotPasswordRequest(@Email String email) {}
  public record ResetPasswordRequest(@Email String email, @NotBlank String otp, @Size(min=8) String newPassword) {}
}

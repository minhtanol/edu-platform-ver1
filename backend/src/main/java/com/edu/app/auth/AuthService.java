package com.edu.app.auth;

import com.edu.app.security.JwtService;
import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class AuthService {
  private final AuthenticationManager auth; private final UserRepository users; private final JwtService jwt; private final RefreshTokenRepository refreshTokens; private final StringRedisTemplate redis; private final PasswordEncoder encoder;
  @Value("${app.jwt.refresh-days}") private long refreshDays;
  @Transactional public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {
    auth.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
    var u = users.findByEmailAndDeletedAtIsNull(req.email()).orElseThrow();
    var rt = new RefreshToken(); rt.setUser(u); rt.setToken(UUID.randomUUID().toString()); rt.setExpiresAt(Instant.now().plus(Duration.ofDays(refreshDays))); refreshTokens.save(rt);
    return tokens(u, rt.getToken());
  }
  @Transactional public AuthDtos.TokenResponse refresh(String token) {
    var rt = refreshTokens.findByTokenAndRevokedAtIsNull(token).filter(t -> t.getExpiresAt().isAfter(Instant.now())).orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    return tokens(rt.getUser(), rt.getToken());
  }
  @Transactional public void logout(String token) { refreshTokens.findByTokenAndRevokedAtIsNull(token).ifPresent(t -> t.setRevokedAt(Instant.now())); }
  public void forgot(String email) { var otp = "%06d".formatted(new SecureRandom().nextInt(1_000_000)); redis.opsForValue().set("otp:" + email, otp, 10, TimeUnit.MINUTES); }
  @Transactional public void reset(AuthDtos.ResetPasswordRequest req) {
    var otp = redis.opsForValue().get("otp:" + req.email()); if (!req.otp().equals(otp)) throw new IllegalArgumentException("Invalid OTP");
    var u = users.findByEmailAndDeletedAtIsNull(req.email()).orElseThrow(); u.setPasswordHash(encoder.encode(req.newPassword())); redis.delete("otp:" + req.email());
  }
  private AuthDtos.TokenResponse tokens(com.edu.app.user.User u, String refreshToken) {
    return new AuthDtos.TokenResponse(jwt.createAccessToken(u), refreshToken, u.getId(), u.getEmail(), u.getFullName(), u.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()));
  }
}

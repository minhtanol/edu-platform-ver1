package com.edu.app.security;

import com.edu.app.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
  @Value("${app.jwt.secret}") private String secret;
  @Value("${app.jwt.access-minutes}") private long accessMinutes;
  private javax.crypto.SecretKey key() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
  public String createAccessToken(User user) {
    var roles = user.getRoles().stream().map(r -> r.getName().name()).toList();
    return Jwts.builder().subject(user.getEmail()).claim("uid", user.getId().toString()).claim("roles", roles)
      .issuedAt(new Date()).expiration(Date.from(Instant.now().plusSeconds(accessMinutes * 60))).signWith(key()).compact();
  }
  public String subject(String token) { return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject(); }
}

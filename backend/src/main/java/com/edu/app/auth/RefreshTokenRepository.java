package com.edu.app.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByTokenAndRevokedAtIsNull(String token);
}

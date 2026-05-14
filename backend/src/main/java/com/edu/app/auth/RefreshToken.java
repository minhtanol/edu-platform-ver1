package com.edu.app.auth;

import com.edu.app.common.BaseEntity;
import com.edu.app.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name="refresh_tokens")
@Getter @Setter
public class RefreshToken extends BaseEntity {
  @ManyToOne(optional=false) private User user;
  @Column(nullable=false, unique=true) private String token;
  @Column(nullable=false) private Instant expiresAt;
  private Instant revokedAt;
}

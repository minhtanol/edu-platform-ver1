package com.edu.app.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity {
  @Id @GeneratedValue @UuidGenerator private UUID id;
  @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
  @Column(nullable = false) private Instant updatedAt = Instant.now();
  private Instant deletedAt;
  @PreUpdate void touch() { updatedAt = Instant.now(); }
}

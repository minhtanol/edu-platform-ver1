package com.edu.app.admin;

import com.edu.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="audit_logs")
@Getter @Setter
public class AuditLog extends BaseEntity {
  private String actorEmail;
  @Column(nullable=false, length = 120) private String action;
  @Column(length = 120) private String entityType;
  @Column(length = 120) private String entityId;
  @Column(length=4000) private String metadata;
}

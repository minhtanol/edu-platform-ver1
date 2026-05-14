package com.edu.app.media;

import com.edu.app.common.BaseEntity;
import com.edu.app.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity @Table(name="media")
@Getter @Setter
public class Media extends BaseEntity {
  public enum MediaType { VIDEO, IMAGE }
  public enum Status { PENDING, APPROVED, REJECTED }
  @ManyToOne(optional=false) private User owner;
  @ManyToOne(optional=false) private User uploadedBy;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length = 20) private MediaType type;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length = 20) private Status status = Status.PENDING;
  @Column(nullable=false) private String title;
  @Column(length = 2000) private String description;
  @Column(nullable=false, length = 1000) private String storagePath;
  @Column(length = 120) private String contentType;
  private long sizeBytes;
}

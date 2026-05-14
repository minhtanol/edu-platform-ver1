package com.edu.app.notification;

import com.edu.app.common.BaseEntity;
import com.edu.app.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="notifications")
@Getter @Setter
public class Notification extends BaseEntity {
  @ManyToOne(optional=false) private User user;
  @Column(nullable=false) private String title;
  @Column(nullable=false, length = 2000) private String body;
  @Column(name = "is_read", nullable=false) private boolean read;
}

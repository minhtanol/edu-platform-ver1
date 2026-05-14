package com.edu.app.user;

import com.edu.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student_profile_histories")
@Getter
@Setter
public class StudentProfileHistory extends BaseEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "student_user_id")
  private User student;

  @ManyToOne
  @JoinColumn(name = "actor_user_id")
  private User actor;

  @Column(length = 8000)
  private String beforeValue;

  @Column(nullable = false, length = 8000)
  private String afterValue;
}

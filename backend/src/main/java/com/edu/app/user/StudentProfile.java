package com.edu.app.user;

import com.edu.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
public class StudentProfile extends BaseEntity {
  @OneToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(length = 1000) private String address;
  private String guardianName;
  private String guardianPhone;
  private String hometown;
  @Column(length = 1000) private String allergies;
}

package com.edu.app.user;

import com.edu.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
public class StudentProfile extends BaseEntity {
  public enum Gender { MALE, FEMALE, OTHER }
  public enum StudyStatus { ACTIVE, INACTIVE }

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(length = 1000) private String address;
  private String guardianName;
  private String guardianPhone;
  private String hometown;
  @Column(length = 1000) private String allergies;
  private LocalDate dateOfBirth;
  @Enumerated(EnumType.STRING) @Column(length = 20) private Gender gender;
  private String emergencyContactName;
  private String emergencyContactPhone;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private StudyStatus studyStatus = StudyStatus.ACTIVE;
}

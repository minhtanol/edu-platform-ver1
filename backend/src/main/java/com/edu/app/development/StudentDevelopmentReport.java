package com.edu.app.development;

import com.edu.app.common.BaseEntity;
import com.edu.app.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "student_development_reports")
@Getter
@Setter
public class StudentDevelopmentReport extends BaseEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id")
  private User student;

  @ManyToOne(optional = false)
  @JoinColumn(name = "teacher_id")
  private User teacher;

  @Column(nullable = false)
  private LocalDate weekStart;

  @Column(length = 2000) private String physicalChange;
  @Column(length = 2000) private String cognitiveChange;
  @Column(length = 2000) private String socialChange;
  @Column(length = 2000) private String emotionalChange;
  @Column(nullable = false, length = 4000) private String note;
}

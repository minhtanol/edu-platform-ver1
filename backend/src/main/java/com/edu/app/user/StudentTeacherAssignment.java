package com.edu.app.user;

import com.edu.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student_teacher_assignments")
@Getter
@Setter
public class StudentTeacherAssignment extends BaseEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "student_user_id")
  private User student;

  @ManyToOne(optional = false)
  @JoinColumn(name = "teacher_user_id")
  private User teacher;
}

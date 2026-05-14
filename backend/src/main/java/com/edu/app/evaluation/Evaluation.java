package com.edu.app.evaluation;

import com.edu.app.common.BaseEntity;
import com.edu.app.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="evaluations")
@Getter @Setter
public class Evaluation extends BaseEntity {
  @ManyToOne(optional=false) private User student;
  @ManyToOne(optional=false) private User teacher;
  @Column(nullable=false) private String subject;
  @Column(nullable=false, length=4000) private String content;
  private Integer score;
}

package com.edu.app.evaluation;

import com.edu.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="evaluation_histories")
@Getter @Setter
public class EvaluationHistory extends BaseEntity {
  @ManyToOne(optional=false) private Evaluation evaluation;
  @Column(nullable=false, length=4000) private String previousContent;
  private Integer previousScore;
}

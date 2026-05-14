package com.edu.app.evaluation;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
  Page<Evaluation> findByStudentIdAndDeletedAtIsNull(UUID studentId, Pageable pageable);
}

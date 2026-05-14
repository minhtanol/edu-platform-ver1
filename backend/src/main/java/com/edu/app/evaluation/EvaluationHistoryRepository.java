package com.edu.app.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EvaluationHistoryRepository extends JpaRepository<EvaluationHistory, UUID> {}

package com.edu.app.evaluation;

import jakarta.validation.constraints.*;
import java.util.UUID;

public class EvaluationDtos {
  public record EvaluationRequest(@NotNull UUID studentId, @NotBlank String subject, @NotBlank String content, @Min(0) @Max(100) Integer score) {}
  public record EvaluationResponse(UUID id, UUID studentId, UUID teacherId, String subject, String content, Integer score) {}
}

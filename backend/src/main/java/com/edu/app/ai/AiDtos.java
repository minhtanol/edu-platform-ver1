package com.edu.app.ai;

import jakarta.validation.constraints.NotBlank;

public class AiDtos {
  public record ChatRequest(@NotBlank String message) {}
  public record ChatResponse(String message) {}
  public record SuggestEvaluationRequest(@NotBlank String studentName, @NotBlank String evidence) {}
}

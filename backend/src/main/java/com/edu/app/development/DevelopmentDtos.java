package com.edu.app.development;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public class DevelopmentDtos {
  public record WeeklyReportRequest(@NotNull UUID studentId, @NotNull LocalDate weekStart, String physicalChange, String cognitiveChange, String socialChange, String emotionalChange, @NotBlank String note) {}
  public record WeeklyReportResponse(UUID id, UUID studentId, String studentName, UUID teacherId, String teacherName, LocalDate weekStart, String physicalChange, String cognitiveChange, String socialChange, String emotionalChange, String note) {}
}

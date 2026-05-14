package com.edu.app.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AdminDtos {
  public record DashboardStats(
    long users,
    long students,
    long teachers,
    MediaStats media,
    long evaluationsThisWeek,
    long developmentReportsThisWeek,
    List<TeacherStudentCount> studentsByTeacher,
    List<StudentMissingEvaluation> studentsMissingEvaluationThisWeek,
    LocalDate weekStart,
    LocalDate weekEnd
  ) {}
  public record MediaStats(long uploaded, long approved, long rejected, long pending) {}
  public record TeacherStudentCount(UUID teacherId, String teacherName, long studentCount) {}
  public record StudentMissingEvaluation(UUID studentId, String studentName, UUID teacherId, String teacherName) {}
}

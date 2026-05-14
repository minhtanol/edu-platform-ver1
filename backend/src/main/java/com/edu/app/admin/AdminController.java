package com.edu.app.admin;

import com.edu.app.common.ApiResponse;
import com.edu.app.development.StudentDevelopmentReportRepository;
import com.edu.app.evaluation.EvaluationRepository;
import com.edu.app.media.*;
import com.edu.app.user.RoleName;
import com.edu.app.user.StudentTeacherAssignmentRepository;
import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/admin") @RequiredArgsConstructor @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
  private final UserRepository users; private final MediaRepository media; private final MediaService mediaService; private final EvaluationRepository evaluations; private final StudentDevelopmentReportRepository developmentReports; private final StudentTeacherAssignmentRepository assignments;
  @GetMapping("/statistics") public ApiResponse<AdminDtos.DashboardStats> stats() {
    var zone = ZoneId.of("Asia/Bangkok");
    var weekStart = LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    var weekEnd = weekStart.plusDays(7);
    var start = weekStart.atStartOfDay(zone).toInstant();
    var end = weekEnd.atStartOfDay(zone).toInstant();
    var mediaStats = new AdminDtos.MediaStats(
      media.count(),
      media.countByStatusAndDeletedAtIsNull(Media.Status.APPROVED),
      media.countByStatusAndDeletedAtIsNull(Media.Status.REJECTED),
      media.countByStatusAndDeletedAtIsNull(Media.Status.PENDING)
    );
    return ApiResponse.ok(new AdminDtos.DashboardStats(
      users.count(),
      users.countByRoles_NameAndDeletedAtIsNull(RoleName.STUDENT),
      users.countByRoles_NameAndDeletedAtIsNull(RoleName.TEACHER),
      mediaStats,
      evaluations.countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndDeletedAtIsNull(start, end),
      developmentReports.countByWeekStartAndDeletedAtIsNull(weekStart),
      assignments.countStudentsByTeacher(),
      evaluations.findStudentsMissingEvaluationThisWeek(start, end),
      weekStart,
      weekEnd.minusDays(1)
    ));
  }
  @GetMapping("/pending-approvals") public ApiResponse<?> pending(@RequestParam(defaultValue="0") int page) { return ApiResponse.ok(mediaService.pending(PageRequest.of(page, 20))); }
  @PostMapping("/pending-approvals/{id}") public ApiResponse<MediaDtos.MediaResponse> approve(@PathVariable UUID id, @RequestBody MediaDtos.ApprovalRequest req) { return ApiResponse.ok(mediaService.approve(id, req.status())); }
}

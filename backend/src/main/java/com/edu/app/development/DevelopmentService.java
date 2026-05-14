package com.edu.app.development;

import com.edu.app.user.RoleName;
import com.edu.app.user.StudentTeacherAssignmentRepository;
import com.edu.app.user.User;
import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DevelopmentService {
  private final StudentDevelopmentReportRepository reports;
  private final UserRepository users;
  private final StudentTeacherAssignmentRepository assignments;

  @Transactional
  public DevelopmentDtos.WeeklyReportResponse saveWeekly(String teacherEmail, DevelopmentDtos.WeeklyReportRequest req) {
    var teacher = users.findByEmailAndDeletedAtIsNull(teacherEmail).orElseThrow();
    if (!hasRole(teacher, RoleName.ADMIN) && !assignments.existsByStudentIdAndTeacherIdAndDeletedAtIsNull(req.studentId(), teacher.getId())) {
      throw new IllegalArgumentException("Bạn không có quyền đánh giá học sinh này");
    }
    var student = users.findById(req.studentId()).orElseThrow();
    var report = reports.findByStudentIdAndTeacherIdAndWeekStartAndDeletedAtIsNull(student.getId(), teacher.getId(), req.weekStart()).orElseGet(StudentDevelopmentReport::new);
    report.setStudent(student);
    report.setTeacher(teacher);
    report.setWeekStart(req.weekStart());
    report.setPhysicalChange(req.physicalChange());
    report.setCognitiveChange(req.cognitiveChange());
    report.setSocialChange(req.socialChange());
    report.setEmotionalChange(req.emotionalChange());
    report.setNote(req.note());
    return toDto(reports.save(report));
  }

  public Page<DevelopmentDtos.WeeklyReportResponse> list(Pageable pageable) {
    return reports.findByDeletedAtIsNullOrderByWeekStartDesc(pageable).map(this::toDto);
  }

  private boolean hasRole(User user, RoleName role) { return user.getRoles().stream().anyMatch(r -> r.getName() == role); }

  private DevelopmentDtos.WeeklyReportResponse toDto(StudentDevelopmentReport r) {
    return new DevelopmentDtos.WeeklyReportResponse(
      r.getId(),
      r.getStudent().getId(),
      r.getStudent().getFullName(),
      r.getTeacher().getId(),
      r.getTeacher().getFullName(),
      r.getWeekStart(),
      r.getPhysicalChange(),
      r.getCognitiveChange(),
      r.getSocialChange(),
      r.getEmotionalChange(),
      r.getNote()
    );
  }
}

package com.edu.app.development;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentDevelopmentReportRepository extends JpaRepository<StudentDevelopmentReport, UUID> {
  Page<StudentDevelopmentReport> findByDeletedAtIsNullOrderByWeekStartDesc(Pageable pageable);
  Optional<StudentDevelopmentReport> findByStudentIdAndTeacherIdAndWeekStartAndDeletedAtIsNull(UUID studentId, UUID teacherId, LocalDate weekStart);
  long countByWeekStartAndDeletedAtIsNull(LocalDate weekStart);
}

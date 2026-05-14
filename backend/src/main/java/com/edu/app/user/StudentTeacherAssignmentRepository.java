package com.edu.app.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentTeacherAssignmentRepository extends JpaRepository<StudentTeacherAssignment, UUID> {
  Optional<StudentTeacherAssignment> findByStudentIdAndDeletedAtIsNull(UUID studentId);
  boolean existsByStudentIdAndTeacherIdAndDeletedAtIsNull(UUID studentId, UUID teacherId);
  void deleteByStudentId(UUID studentId);
}

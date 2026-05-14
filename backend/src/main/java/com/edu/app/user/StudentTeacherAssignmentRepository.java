package com.edu.app.user;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentTeacherAssignmentRepository extends JpaRepository<StudentTeacherAssignment, UUID> {
  Optional<StudentTeacherAssignment> findByStudentIdAndDeletedAtIsNull(UUID studentId);
  boolean existsByStudentIdAndTeacherIdAndDeletedAtIsNull(UUID studentId, UUID teacherId);
  long countByTeacherIdAndDeletedAtIsNull(UUID teacherId);
  @Query("""
    select new com.edu.app.admin.AdminDtos$TeacherStudentCount(t.id, t.fullName, count(a.id))
    from User t
    join t.roles tr
    left join StudentTeacherAssignment a on a.teacher = t and a.deletedAt is null
    where tr.name = com.edu.app.user.RoleName.TEACHER
      and t.deletedAt is null
    group by t.id, t.fullName
    order by t.fullName
  """)
  List<com.edu.app.admin.AdminDtos.TeacherStudentCount> countStudentsByTeacher();
  void deleteByStudentId(UUID studentId);
}

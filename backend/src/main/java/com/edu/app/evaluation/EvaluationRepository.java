package com.edu.app.evaluation;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
  Page<Evaluation> findByStudentIdAndDeletedAtIsNull(UUID studentId, Pageable pageable);
  long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndDeletedAtIsNull(Instant start, Instant end);
  @Query("""
    select new com.edu.app.admin.AdminDtos$StudentMissingEvaluation(s.id, s.fullName, t.id, t.fullName)
    from User s
    join s.roles sr
    left join StudentTeacherAssignment a on a.student = s and a.deletedAt is null
    left join User t on a.teacher = t
    where sr.name = com.edu.app.user.RoleName.STUDENT
      and s.deletedAt is null
      and not exists (
        select e.id from Evaluation e
        where e.student = s
          and e.deletedAt is null
          and e.createdAt >= :start
          and e.createdAt < :end
      )
    order by s.fullName
  """)
  List<com.edu.app.admin.AdminDtos.StudentMissingEvaluation> findStudentsMissingEvaluationThisWeek(@Param("start") Instant start, @Param("end") Instant end);
}

package com.edu.app.user;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmailAndDeletedAtIsNull(String email);
  Page<User> findByDeletedAtIsNull(Pageable pageable);
  Page<User> findByRoles_NameAndDeletedAtIsNull(RoleName role, Pageable pageable);
  long countByRoles_NameAndDeletedAtIsNull(RoleName role);
  @Query("""
    select u from User u
    join StudentTeacherAssignment a on a.student = u
    where a.teacher.id = :teacherId
      and a.deletedAt is null
      and u.deletedAt is null
  """)
  Page<User> findAssignedStudents(@Param("teacherId") UUID teacherId, Pageable pageable);
}

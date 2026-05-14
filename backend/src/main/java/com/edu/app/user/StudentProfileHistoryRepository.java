package com.edu.app.user;

import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileHistoryRepository extends JpaRepository<StudentProfileHistory, UUID> {
  Page<StudentProfileHistory> findByStudentIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID studentId, Pageable pageable);
}

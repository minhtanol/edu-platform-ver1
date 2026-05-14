package com.edu.app.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
  Optional<StudentProfile> findByUserIdAndDeletedAtIsNull(UUID userId);
  void deleteByUserId(UUID userId);
}

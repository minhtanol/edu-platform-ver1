package com.edu.app.user;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmailAndDeletedAtIsNull(String email);
  Page<User> findByDeletedAtIsNull(Pageable pageable);
  Page<User> findByRoles_NameAndDeletedAtIsNull(RoleName role, Pageable pageable);
}

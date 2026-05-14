package com.edu.app.media;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MediaRepository extends JpaRepository<Media, UUID> {
  Page<Media> findByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);
  Page<Media> findByStatusAndDeletedAtIsNull(Media.Status status, Pageable pageable);
}

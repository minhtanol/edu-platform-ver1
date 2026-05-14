package com.edu.app.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {
  Optional<ChatConversation> findFirstByUserEmailOrderByCreatedAtDesc(String email);
}

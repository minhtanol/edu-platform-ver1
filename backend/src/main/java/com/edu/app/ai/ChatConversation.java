package com.edu.app.ai;

import com.edu.app.common.BaseEntity;
import com.edu.app.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="chat_conversations")
@Getter @Setter
public class ChatConversation extends BaseEntity {
  @ManyToOne(optional=false) private User user;
  @Column(nullable=false) private String title = "AI Study Chat";
}

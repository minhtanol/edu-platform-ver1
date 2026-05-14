package com.edu.app.ai;

import com.edu.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="chat_messages")
@Getter @Setter
public class ChatMessage extends BaseEntity {
  public enum Sender { USER, ASSISTANT }
  @ManyToOne(optional=false) private ChatConversation conversation;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length = 20) private Sender sender;
  @Column(nullable=false, length=8000) private String content;
}

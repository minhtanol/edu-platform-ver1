package com.edu.app.ai;

import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.util.*;

@Service @RequiredArgsConstructor
public class AiService {
  private final UserRepository users; private final ChatConversationRepository conversations; private final ChatMessageRepository messages;
  @Value("${app.deepseek.api-key}") private String apiKey; @Value("${app.deepseek.base-url}") private String baseUrl;
  @Transactional public String chat(String email, String prompt) {
    var user = users.findByEmailAndDeletedAtIsNull(email).orElseThrow();
    var c = conversations.findFirstByUserEmailOrderByCreatedAtDesc(email).orElseGet(() -> { var x = new ChatConversation(); x.setUser(user); return conversations.save(x); });
    save(c, ChatMessage.Sender.USER, prompt);
    var reply = callDeepSeek("Bạn là trợ lý học tập tiếng Việt, trả lời ngắn gọn và chính xác.", prompt);
    save(c, ChatMessage.Sender.ASSISTANT, reply);
    return reply;
  }
  public String suggest(String name, String evidence) { return callDeepSeek("Hãy gợi ý nhận xét đánh giá học sinh theo giọng chuyên nghiệp.", "Học sinh: " + name + "\nMinh chứng: " + evidence); }
  public String summarize(String progress) { return callDeepSeek("Tóm tắt tiến độ học tập và đề xuất hành động.", progress); }
  private void save(ChatConversation c, ChatMessage.Sender sender, String content) { var m = new ChatMessage(); m.setConversation(c); m.setSender(sender); m.setContent(content); messages.save(m); }
  @SuppressWarnings("unchecked") private String callDeepSeek(String system, String user) {
    if (apiKey == null || apiKey.isBlank()) return "AI demo mode: " + user;
    var body = Map.of("model","deepseek-chat","messages",List.of(Map.of("role","system","content",system), Map.of("role","user","content",user)));
    var res = RestClient.create(baseUrl).post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer " + apiKey).body(body).retrieve().body(Map.class);
    var choices = (List<Map<String,Object>>) res.get("choices"); var msg = (Map<String,Object>) choices.getFirst().get("message"); return String.valueOf(msg.get("content"));
  }
}

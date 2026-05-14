package com.edu.app.ai;

import com.edu.app.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/ai") @RequiredArgsConstructor
public class AiController {
  private final AiService service;
  @PostMapping("/chat") public ApiResponse<AiDtos.ChatResponse> chat(Authentication auth, @Valid @RequestBody AiDtos.ChatRequest req) { return ApiResponse.ok(new AiDtos.ChatResponse(service.chat(auth.getName(), req.message()))); }
  @PostMapping("/suggest-evaluation") public ApiResponse<AiDtos.ChatResponse> suggest(@Valid @RequestBody AiDtos.SuggestEvaluationRequest req) { return ApiResponse.ok(new AiDtos.ChatResponse(service.suggest(req.studentName(), req.evidence()))); }
  @PostMapping("/summarize-progress") public ApiResponse<AiDtos.ChatResponse> summarize(@Valid @RequestBody AiDtos.ChatRequest req) { return ApiResponse.ok(new AiDtos.ChatResponse(service.summarize(req.message()))); }
}

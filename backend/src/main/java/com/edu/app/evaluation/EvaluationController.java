package com.edu.app.evaluation;

import com.edu.app.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/evaluations") @RequiredArgsConstructor
public class EvaluationController {
  private final EvaluationService service;
  @PostMapping @PreAuthorize("hasAnyRole('TEACHER','ADMIN')") public ApiResponse<EvaluationDtos.EvaluationResponse> create(Authentication auth, @Valid @RequestBody EvaluationDtos.EvaluationRequest req) { return ApiResponse.created(service.create(auth.getName(), req)); }
  @GetMapping("/student/{studentId}") public ApiResponse<Page<EvaluationDtos.EvaluationResponse>> byStudent(@PathVariable UUID studentId, @RequestParam(defaultValue="0") int page) { return ApiResponse.ok(service.byStudent(studentId, PageRequest.of(page, 20))); }
  @PutMapping("/{id}") @PreAuthorize("hasAnyRole('TEACHER','ADMIN')") public ApiResponse<EvaluationDtos.EvaluationResponse> update(@PathVariable UUID id, @Valid @RequestBody EvaluationDtos.EvaluationRequest req) { return ApiResponse.ok(service.update(id, req)); }
  @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('TEACHER','ADMIN')") public ApiResponse<Void> delete(@PathVariable UUID id) { service.delete(id); return ApiResponse.ok(null); }
}

package com.edu.app.development;

import com.edu.app.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/development-reports")
@RequiredArgsConstructor
public class DevelopmentController {
  private final DevelopmentService service;

  @PostMapping
  @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
  public ApiResponse<DevelopmentDtos.WeeklyReportResponse> save(Authentication auth, @Valid @RequestBody DevelopmentDtos.WeeklyReportRequest req) {
    return ApiResponse.created(service.saveWeekly(auth.getName(), req));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Page<DevelopmentDtos.WeeklyReportResponse>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
    return ApiResponse.ok(service.list(PageRequest.of(page, size)));
  }
}

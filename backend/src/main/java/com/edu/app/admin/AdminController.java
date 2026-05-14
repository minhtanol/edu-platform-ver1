package com.edu.app.admin;

import com.edu.app.common.ApiResponse;
import com.edu.app.media.*;
import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/admin") @RequiredArgsConstructor @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
  private final UserRepository users; private final MediaRepository media; private final MediaService mediaService;
  @GetMapping("/statistics") public ApiResponse<Map<String, Long>> stats() {
    return ApiResponse.ok(Map.of("users", users.count(), "media", media.count(), "pendingApprovals", media.findByStatusAndDeletedAtIsNull(Media.Status.PENDING, PageRequest.of(0,1)).getTotalElements()));
  }
  @GetMapping("/pending-approvals") public ApiResponse<?> pending(@RequestParam(defaultValue="0") int page) { return ApiResponse.ok(mediaService.pending(PageRequest.of(page, 20))); }
  @PostMapping("/pending-approvals/{id}") public ApiResponse<MediaDtos.MediaResponse> approve(@PathVariable UUID id, @RequestBody MediaDtos.ApprovalRequest req) { return ApiResponse.ok(mediaService.approve(id, req.status())); }
}

package com.edu.app.media;

import com.edu.app.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/media") @RequiredArgsConstructor
public class MediaController {
  private final MediaService service;
  @PostMapping("/upload") @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
  public ApiResponse<MediaDtos.MediaResponse> upload(Authentication auth, @RequestParam UUID ownerId, @RequestParam String title, @RequestParam(required=false) String description, @RequestParam Media.MediaType type, @RequestPart MultipartFile file) throws Exception {
    return ApiResponse.created(service.upload(auth.getName(), ownerId, title, description, type, file));
  }
  @GetMapping("/student/{studentId}") public ApiResponse<Page<MediaDtos.MediaResponse>> byStudent(@PathVariable UUID studentId, @RequestParam(defaultValue="0") int page) { return ApiResponse.ok(service.byStudent(studentId, PageRequest.of(page, 20))); }
  @GetMapping("/stream/{id}") public ResponseEntity<Resource> stream(@PathVariable UUID id) {
    var media = service.findActive(id).orElseThrow();
    var contentType = media.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(media.getContentType());
    return ResponseEntity.ok().contentType(contentType).body(service.stream(id));
  }
}

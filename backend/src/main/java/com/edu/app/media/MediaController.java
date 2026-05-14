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
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/media") @RequiredArgsConstructor
public class MediaController {
  private final MediaService service;
  @PostMapping("/upload") @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
  public ApiResponse<MediaDtos.MediaResponse> upload(Authentication auth, @RequestParam UUID ownerId, @RequestParam String title, @RequestParam(required=false) String description, @RequestParam Media.MediaType type, @RequestPart MultipartFile file) throws Exception {
    return ApiResponse.created(service.upload(auth.getName(), ownerId, title, description, type, file));
  }
  @GetMapping @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Page<MediaDtos.MediaResponse>> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="100") int size) { return ApiResponse.ok(service.list(PageRequest.of(page, size))); }
  @GetMapping("/student/{studentId}") public ApiResponse<Page<MediaDtos.MediaResponse>> byStudent(Authentication auth, @PathVariable UUID studentId, @RequestParam(defaultValue="0") int page) { return ApiResponse.ok(service.byStudent(auth.getName(), studentId, PageRequest.of(page, 20))); }
  @GetMapping("/stream/{id}") public ResponseEntity<Resource> stream(Authentication auth, @PathVariable UUID id, @RequestHeader HttpHeaders headers) {
    service.findActive(id).orElseThrow();
    var result = service.stream(auth.getName(), id, headers.getRange(), false);
    var response = ResponseEntity.status(result.partial() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
      .contentType(MediaType.parseMediaType(result.contentType()))
      .contentLength(result.contentLength())
      .header(HttpHeaders.ACCEPT_RANGES, "bytes")
      .cacheControl(CacheControl.noCache());
    if (result.partial()) response.header(HttpHeaders.CONTENT_RANGE, "bytes " + result.start() + "-" + result.end() + "/" + result.totalLength());
    return response.body(result.resource());
  }
  @GetMapping("/download/{id}") @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<Resource> download(Authentication auth, @PathVariable UUID id) {
    service.findActive(id).orElseThrow();
    var result = service.stream(auth.getName(), id, java.util.List.of(), true);
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(result.contentType()))
      .contentLength(result.contentLength())
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(result.filename(), StandardCharsets.UTF_8).build().toString())
      .body(result.resource());
  }
}

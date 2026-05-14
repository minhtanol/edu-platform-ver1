package com.edu.app.settings;

import com.edu.app.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/branding")
@RequiredArgsConstructor
public class BrandingController {
  private final BrandingService service;

  @GetMapping
  public ApiResponse<BrandingDtos.BrandingResponse> getBranding() {
    return ApiResponse.ok(service.getBranding());
  }

  @GetMapping("/logo")
  public ResponseEntity<Resource> logo() {
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(service.logoContentType()))
      .cacheControl(CacheControl.noCache())
      .body(service.logoResource());
  }

  @PostMapping("/logo")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<BrandingDtos.BrandingResponse> updateLogo(@RequestPart MultipartFile file) throws Exception {
    return ApiResponse.ok(service.updateLogo(file));
  }
}

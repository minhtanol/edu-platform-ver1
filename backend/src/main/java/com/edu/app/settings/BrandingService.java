package com.edu.app.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandingService {
  private static final String APP_NAME = "app.name";
  private static final String LOGO_PATH = "branding.logo.path";
  private static final String LOGO_CONTENT_TYPE = "branding.logo.content-type";
  private static final String LOGO_VERSION = "branding.logo.version";
  private final AppSettingRepository settings;
  @Value("${app.upload-dir}") private String uploadDir;

  public BrandingDtos.BrandingResponse getBranding() {
    var appName = get(APP_NAME).orElse("Nền tảng học tập");
    var version = get(LOGO_VERSION).orElse(null);
    var logoUrl = version == null ? null : "/api/v1/branding/logo?v=" + version;
    return new BrandingDtos.BrandingResponse(appName, logoUrl);
  }

  @Transactional
  public BrandingDtos.BrandingResponse updateLogo(MultipartFile file) throws Exception {
    if (file.isEmpty()) throw new IllegalArgumentException("Logo không được để trống");
    var contentType = Optional.ofNullable(file.getContentType()).orElse("");
    if (!contentType.startsWith("image/")) throw new IllegalArgumentException("Logo phải là file hình ảnh");
    var ext = extension(file.getOriginalFilename(), contentType);
    var dir = Path.of(uploadDir, "branding");
    Files.createDirectories(dir);
    var target = dir.resolve("logo" + ext);
    file.transferTo(target);
    set(LOGO_PATH, target.toString());
    set(LOGO_CONTENT_TYPE, contentType);
    set(LOGO_VERSION, String.valueOf(Instant.now().toEpochMilli()));
    return getBranding();
  }

  public Resource logoResource() {
    return new FileSystemResource(get(LOGO_PATH).orElseThrow());
  }

  public String logoContentType() {
    return get(LOGO_CONTENT_TYPE).orElse("image/png");
  }

  private Optional<String> get(String key) {
    return settings.findById(key).map(AppSetting::getValue).filter(v -> v != null && !v.isBlank());
  }

  private void set(String key, String value) {
    var setting = settings.findById(key).orElseGet(() -> {
      var s = new AppSetting(); s.setKey(key); return s;
    });
    setting.setValue(value);
    settings.save(setting);
  }

  private String extension(String filename, String contentType) {
    var name = Optional.ofNullable(filename).orElse("").toLowerCase();
    if (name.endsWith(".svg")) return ".svg";
    if (name.endsWith(".webp")) return ".webp";
    if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return ".jpg";
    if (contentType.equals("image/svg+xml")) return ".svg";
    if (contentType.equals("image/webp")) return ".webp";
    if (contentType.equals("image/jpeg")) return ".jpg";
    return ".png";
  }
}

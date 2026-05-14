package com.edu.app.media;

import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.net.URI;
import java.nio.file.*;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class MediaService implements InitializingBean {
  private final MediaRepository media; private final UserRepository users;
  @Value("${app.upload-dir}") private String uploadDir;
  @Value("${app.storage.provider:local}") private String storageProvider;
  @Value("${cloudflare.r2.account-id:}") private String r2AccountId;
  @Value("${cloudflare.r2.access-key:}") private String r2AccessKey;
  @Value("${cloudflare.r2.secret-key:}") private String r2SecretKey;
  @Value("${cloudflare.r2.bucket-name:}") private String r2BucketName;
  @Value("${cloudflare.r2.public-url:}") private String r2PublicUrl;
  @Value("${cloudflare.r2.endpoint:}") private String r2Endpoint;
  @Value("${cloudflare.r2.region:auto}") private String r2Region;
  private S3Client s3;

  @Override public void afterPropertiesSet() {
    if (!isR2()) return;
    if (r2AccountId.isBlank() || r2BucketName.isBlank() || r2AccessKey.isBlank() || r2SecretKey.isBlank()) {
      throw new IllegalStateException("R2 storage is enabled but account id, bucket or credentials are missing");
    }
    s3 = S3Client.builder()
      .endpointOverride(URI.create(r2Endpoint()))
      .region(Region.of(r2Region))
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(r2AccessKey, r2SecretKey)))
      .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
      .build();
  }

  @Transactional public MediaDtos.MediaResponse upload(String uploaderEmail, UUID ownerId, String title, String description, Media.MediaType type, MultipartFile file) throws Exception {
    var uploader = users.findByEmailAndDeletedAtIsNull(uploaderEmail).orElseThrow(); var owner = users.findById(ownerId).orElseThrow();
    var storagePath = isR2() ? uploadToR2(ownerId, file) : uploadToLocal(file);
    var m = new Media(); m.setUploadedBy(uploader); m.setOwner(owner); m.setTitle(title); m.setDescription(description); m.setType(type); m.setStoragePath(storagePath); m.setContentType(file.getContentType()); m.setSizeBytes(file.getSize()); return toDto(media.save(m));
  }
  public Page<MediaDtos.MediaResponse> byStudent(UUID studentId, Pageable p) { return media.findByOwnerIdAndDeletedAtIsNull(studentId, p).map(this::toDto); }
  public Page<MediaDtos.MediaResponse> pending(Pageable p) { return media.findByStatusAndDeletedAtIsNull(Media.Status.PENDING, p).map(this::toDto); }
  @Transactional public MediaDtos.MediaResponse approve(UUID id, Media.Status status) { var m = media.findById(id).orElseThrow(); m.setStatus(status); return toDto(m); }
  public Resource stream(UUID id) {
    var m = media.findById(id).orElseThrow();
    if (isR2Path(m.getStoragePath())) {
      ResponseInputStream<GetObjectResponse> object = s3.getObject(GetObjectRequest.builder().bucket(r2BucketName).key(r2Key(m.getStoragePath())).build());
      return new InputStreamResource(object);
    }
    return new FileSystemResource(m.getStoragePath());
  }
  public Optional<Media> findActive(UUID id) { return media.findById(id).filter(m -> m.getDeletedAt() == null); }
  private String uploadToLocal(MultipartFile file) throws Exception {
    Files.createDirectories(Path.of(uploadDir));
    var path = Path.of(uploadDir, objectName(file));
    file.transferTo(path);
    return path.toString();
  }
  private String uploadToR2(UUID ownerId, MultipartFile file) throws Exception {
    var key = "media/" + ownerId + "/" + objectName(file);
    var builder = PutObjectRequest.builder()
      .bucket(r2BucketName)
      .key(key)
      .contentLength(file.getSize())
      .build();
    if (file.getContentType() != null) builder = builder.toBuilder().contentType(file.getContentType()).build();
    try (var input = file.getInputStream()) {
      s3.putObject(builder, RequestBody.fromInputStream(input, file.getSize()));
    }
    return "r2://" + r2BucketName + "/" + key;
  }
  private String objectName(MultipartFile file) {
    var original = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.bin");
    var safeName = Path.of(original).getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
    return UUID.randomUUID() + "-" + safeName;
  }
  private boolean isR2() { return "r2".equalsIgnoreCase(storageProvider); }
  private boolean isR2Path(String storagePath) { return storagePath != null && storagePath.toLowerCase(Locale.ROOT).startsWith("r2://"); }
  private String r2Key(String storagePath) {
    var prefix = "r2://" + r2BucketName + "/";
    if (!storagePath.startsWith(prefix)) throw new IllegalArgumentException("Invalid R2 storage path");
    return storagePath.substring(prefix.length());
  }
  private String r2Endpoint() {
    if (!r2Endpoint.isBlank()) return r2Endpoint;
    return "https://" + r2AccountId + ".r2.cloudflarestorage.com";
  }
  private MediaDtos.MediaResponse toDto(Media m) { return new MediaDtos.MediaResponse(m.getId(), m.getOwner().getId(), m.getTitle(), m.getDescription(), m.getType(), m.getStatus(), m.getContentType(), m.getSizeBytes()); }
}

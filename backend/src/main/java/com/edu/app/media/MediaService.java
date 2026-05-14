package com.edu.app.media;

import com.edu.app.user.RoleName;
import com.edu.app.user.StudentTeacherAssignmentRepository;
import com.edu.app.user.User;
import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
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
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.net.URI;
import java.nio.file.*;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class MediaService {
  private final MediaRepository media; private final UserRepository users; private final StudentTeacherAssignmentRepository assignments;
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

  @Transactional public MediaDtos.MediaResponse upload(String uploaderEmail, UUID ownerId, String title, String description, Media.MediaType type, MultipartFile file) throws Exception {
    var uploader = users.findByEmailAndDeletedAtIsNull(uploaderEmail).orElseThrow(); var owner = users.findById(ownerId).orElseThrow();
    ensureCanAccessStudent(uploader, ownerId);
    var storagePath = isR2() ? uploadToR2(ownerId, file) : uploadToLocal(file);
    var m = new Media(); m.setUploadedBy(uploader); m.setOwner(owner); m.setTitle(title); m.setDescription(description); m.setType(type); m.setStoragePath(storagePath); m.setContentType(file.getContentType()); m.setSizeBytes(file.getSize()); return toDto(media.save(m));
  }
  public Page<MediaDtos.MediaResponse> list(Pageable p) { return media.findByDeletedAtIsNull(p).map(this::toDto); }
  public Page<MediaDtos.MediaResponse> byStudent(String requesterEmail, UUID studentId, Pageable p) {
    var requester = users.findByEmailAndDeletedAtIsNull(requesterEmail).orElseThrow();
    ensureCanAccessStudent(requester, studentId);
    return media.findByOwnerIdAndDeletedAtIsNull(studentId, p).map(this::toDto);
  }
  public Page<MediaDtos.MediaResponse> pending(Pageable p) { return media.findByStatusAndDeletedAtIsNull(Media.Status.PENDING, p).map(this::toDto); }
  @Transactional public MediaDtos.MediaResponse approve(UUID id, Media.Status status) { var m = media.findById(id).orElseThrow(); m.setStatus(status); return toDto(m); }
  public Resource stream(String requesterEmail, UUID id) {
    var requester = users.findByEmailAndDeletedAtIsNull(requesterEmail).orElseThrow();
    var m = media.findById(id).orElseThrow();
    ensureCanAccessStudent(requester, m.getOwner().getId());
    if (isR2Path(m.getStoragePath())) {
      try {
        ResponseInputStream<GetObjectResponse> object = r2Client().getObject(GetObjectRequest.builder().bucket(r2BucketName).key(r2Key(m.getStoragePath())).build());
        return new InputStreamResource(object);
      } catch (S3Exception ex) {
        throw new IllegalStateException("Không đọc được file từ R2. Kiểm tra bucket, quyền đọc object và cấu hình R2.", ex);
      }
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
      r2Client().putObject(builder, RequestBody.fromInputStream(input, file.getSize()));
    } catch (S3Exception ex) {
      throw new IllegalStateException("Không upload được file lên R2. Kiểm tra bucket, access key/secret key và quyền ghi object.", ex);
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
    var endpoint = r2Endpoint.trim();
    if (!endpoint.isBlank()) return endpoint.startsWith("http://") || endpoint.startsWith("https://") ? endpoint : "https://" + endpoint;
    var account = r2AccountId.trim();
    if (account.startsWith("http://") || account.startsWith("https://")) return account;
    return "https://" + account + ".r2.cloudflarestorage.com";
  }
  private S3Client r2Client() {
    if (r2AccountId.isBlank() || r2BucketName.isBlank() || r2AccessKey.isBlank() || r2SecretKey.isBlank()) {
      throw new IllegalStateException("R2 storage is enabled but account id, bucket or credentials are missing");
    }
    if (s3 == null) {
      s3 = S3Client.builder()
        .endpointOverride(URI.create(r2Endpoint()))
        .region(Region.of(r2Region))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(r2AccessKey, r2SecretKey)))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .build();
    }
    return s3;
  }
  private boolean hasRole(User user, RoleName role) { return user.getRoles().stream().anyMatch(r -> r.getName() == role); }
  private void ensureCanAccessStudent(User requester, UUID studentId) {
    if (hasRole(requester, RoleName.ADMIN)) return;
    if (hasRole(requester, RoleName.STUDENT) && requester.getId().equals(studentId)) return;
    if (hasRole(requester, RoleName.TEACHER) && assignments.existsByStudentIdAndTeacherIdAndDeletedAtIsNull(studentId, requester.getId())) return;
    throw new IllegalArgumentException("Bạn không có quyền thao tác với học sinh này");
  }
  private MediaDtos.MediaResponse toDto(Media m) {
    return new MediaDtos.MediaResponse(
      m.getId(),
      m.getOwner().getId(),
      m.getOwner().getFullName(),
      m.getUploadedBy().getId(),
      m.getUploadedBy().getFullName(),
      m.getTitle(),
      m.getDescription(),
      m.getType(),
      m.getStatus(),
      m.getContentType(),
      m.getSizeBytes()
    );
  }
}

package com.edu.app.media;

import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class MediaService {
  private final MediaRepository media; private final UserRepository users;
  @Value("${app.upload-dir}") private String uploadDir;
  @Transactional public MediaDtos.MediaResponse upload(String uploaderEmail, UUID ownerId, String title, String description, Media.MediaType type, MultipartFile file) throws Exception {
    var uploader = users.findByEmailAndDeletedAtIsNull(uploaderEmail).orElseThrow(); var owner = users.findById(ownerId).orElseThrow();
    Files.createDirectories(Path.of(uploadDir)); var name = UUID.randomUUID() + "-" + file.getOriginalFilename(); var path = Path.of(uploadDir, name); file.transferTo(path);
    var m = new Media(); m.setUploadedBy(uploader); m.setOwner(owner); m.setTitle(title); m.setDescription(description); m.setType(type); m.setStoragePath(path.toString()); m.setContentType(file.getContentType()); m.setSizeBytes(file.getSize()); return toDto(media.save(m));
  }
  public Page<MediaDtos.MediaResponse> byStudent(UUID studentId, Pageable p) { return media.findByOwnerIdAndDeletedAtIsNull(studentId, p).map(this::toDto); }
  public Page<MediaDtos.MediaResponse> pending(Pageable p) { return media.findByStatusAndDeletedAtIsNull(Media.Status.PENDING, p).map(this::toDto); }
  @Transactional public MediaDtos.MediaResponse approve(UUID id, Media.Status status) { var m = media.findById(id).orElseThrow(); m.setStatus(status); return toDto(m); }
  public Resource stream(UUID id) { var m = media.findById(id).orElseThrow(); return new FileSystemResource(m.getStoragePath()); }
  public Optional<Media> findActive(UUID id) { return media.findById(id).filter(m -> m.getDeletedAt() == null); }
  private MediaDtos.MediaResponse toDto(Media m) { return new MediaDtos.MediaResponse(m.getId(), m.getOwner().getId(), m.getTitle(), m.getDescription(), m.getType(), m.getStatus(), m.getContentType(), m.getSizeBytes()); }
}

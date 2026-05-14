package com.edu.app.media;

import java.util.UUID;
import java.time.Instant;

public class MediaDtos {
  public record MediaResponse(UUID id, UUID ownerId, String ownerName, UUID uploadedById, String uploadedByName, String title, String description, Media.MediaType type, Media.Status status, String contentType, long sizeBytes, Instant approvedAt) {}
  public record ApprovalRequest(Media.Status status) {}
}

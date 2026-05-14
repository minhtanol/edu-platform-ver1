package com.edu.app.media;

import java.util.UUID;

public class MediaDtos {
  public record MediaResponse(UUID id, UUID ownerId, String ownerName, UUID uploadedById, String uploadedByName, String title, String description, Media.MediaType type, Media.Status status, String contentType, long sizeBytes) {}
  public record ApprovalRequest(Media.Status status) {}
}

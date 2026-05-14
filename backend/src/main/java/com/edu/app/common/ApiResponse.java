package com.edu.app.common;

import java.time.Instant;

public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp) {
  public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, "OK", data, Instant.now()); }
  public static <T> ApiResponse<T> created(T data) { return new ApiResponse<>(true, "CREATED", data, Instant.now()); }
  public static ApiResponse<Void> error(String message) { return new ApiResponse<>(false, message, null, Instant.now()); }
}

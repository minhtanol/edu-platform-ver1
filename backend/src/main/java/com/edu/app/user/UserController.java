package com.edu.app.user;

import com.edu.app.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/users") @RequiredArgsConstructor
public class UserController {
  private final UserService service;
  @GetMapping("/me") public ApiResponse<UserDtos.UserResponse> me(Authentication auth) { return ApiResponse.ok(service.me(auth.getName())); }
  @GetMapping("/students") @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
  public ApiResponse<Page<UserDtos.UserResponse>> students(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="100") int size) { return ApiResponse.ok(service.students(PageRequest.of(page, size))); }
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping public ApiResponse<Page<UserDtos.UserResponse>> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return ApiResponse.ok(service.list(PageRequest.of(page, size))); }
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping public ApiResponse<UserDtos.UserResponse> create(@Valid @RequestBody UserDtos.CreateUserRequest req) { return ApiResponse.created(service.create(req)); }
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}") public ApiResponse<UserDtos.UserResponse> update(@PathVariable UUID id, @RequestBody UserDtos.UpdateUserRequest req) { return ApiResponse.ok(service.update(id, req)); }
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable UUID id) { service.delete(id); return ApiResponse.ok(null); }
}

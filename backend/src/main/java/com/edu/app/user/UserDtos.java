package com.edu.app.user;

import jakarta.validation.constraints.*;
import java.util.*;

public class UserDtos {
  public record UserResponse(UUID id, String email, String fullName, boolean enabled, Set<RoleName> roles, UUID teacherId, String teacherName) {}
  public record CreateUserRequest(@Email String email, @Size(min = 8) String password, @NotBlank String fullName, Set<RoleName> roles, UUID teacherId) {}
  public record UpdateUserRequest(String fullName, Boolean enabled, Set<RoleName> roles, UUID teacherId) {}
}

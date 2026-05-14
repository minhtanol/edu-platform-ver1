package com.edu.app.user;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class UserDtos {
  public record UserResponse(UUID id, String email, String fullName, boolean enabled, Set<RoleName> roles, UUID teacherId, String teacherName, String address, String guardianName, String guardianPhone, String hometown, String allergies, LocalDate dateOfBirth, Integer age, StudentProfile.Gender gender, String emergencyContactName, String emergencyContactPhone, StudentProfile.StudyStatus studyStatus) {}
  public record CreateUserRequest(@Email String email, @Size(min = 8) String password, @NotBlank String fullName, Set<RoleName> roles, UUID teacherId, String address, String guardianName, String guardianPhone, String hometown, String allergies, LocalDate dateOfBirth, StudentProfile.Gender gender, String emergencyContactName, String emergencyContactPhone, StudentProfile.StudyStatus studyStatus) {}
  public record UpdateUserRequest(String fullName, Boolean enabled, Set<RoleName> roles, UUID teacherId, String address, String guardianName, String guardianPhone, String hometown, String allergies, LocalDate dateOfBirth, StudentProfile.Gender gender, String emergencyContactName, String emergencyContactPhone, StudentProfile.StudyStatus studyStatus) {}
  public record StudentProfileHistoryResponse(UUID id, UUID studentId, String studentName, UUID actorId, String actorName, String beforeValue, String afterValue, Instant createdAt) {}
}

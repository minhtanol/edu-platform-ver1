package com.edu.app.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class UserService {
  private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder encoder; private final StudentTeacherAssignmentRepository assignments;
  public Page<UserDtos.UserResponse> list(Pageable pageable) { return users.findByDeletedAtIsNull(pageable).map(this::toResponse); }
  public Page<UserDtos.UserResponse> teachers(Pageable pageable) { return users.findByRoles_NameAndDeletedAtIsNull(RoleName.TEACHER, pageable).map(this::toResponse); }
  public Page<UserDtos.UserResponse> students(String requesterEmail, Pageable pageable) {
    var requester = users.findByEmailAndDeletedAtIsNull(requesterEmail).orElseThrow();
    if (hasRole(requester, RoleName.TEACHER) && !hasRole(requester, RoleName.ADMIN)) {
      return users.findAssignedStudents(requester.getId(), pageable).map(this::toResponse);
    }
    return users.findByRoles_NameAndDeletedAtIsNull(RoleName.STUDENT, pageable).map(this::toResponse);
  }
  public UserDtos.UserResponse me(String email) { return users.findByEmailAndDeletedAtIsNull(email).map(this::toResponse).orElseThrow(); }
  @Transactional public UserDtos.UserResponse create(UserDtos.CreateUserRequest req) {
    users.findByEmailAndDeletedAtIsNull(req.email()).ifPresent(existing -> { throw new IllegalArgumentException("Email already exists"); });
    var resolvedRoles = resolve(req.roles());
    var u = new User(); u.setEmail(req.email()); u.setFullName(req.fullName()); u.setPasswordHash(encoder.encode(req.password())); u.setRoles(resolvedRoles);
    var saved = users.save(u);
    syncTeacherAssignment(saved, resolvedRoles, req.teacherId());
    return toResponse(saved);
  }
  @Transactional public UserDtos.UserResponse update(UUID id, UserDtos.UpdateUserRequest req) {
    var u = users.findById(id).orElseThrow(); if (req.fullName()!=null) u.setFullName(req.fullName()); if (req.enabled()!=null) u.setEnabled(req.enabled()); if (req.roles()!=null) u.setRoles(resolve(req.roles())); if (req.roles()!=null || req.teacherId()!=null) syncTeacherAssignment(u, u.getRoles(), req.teacherId()); return toResponse(u);
  }
  @Transactional public void delete(UUID id) { assignments.deleteByStudentId(id); var u = users.findById(id).orElseThrow(); u.setDeletedAt(Instant.now()); }
  private Set<Role> resolve(Set<RoleName> names) { return Optional.ofNullable(names).orElse(Set.of(RoleName.STUDENT)).stream().map(n -> roles.findByName(n).orElseThrow()).collect(Collectors.toSet()); }
  private boolean hasRole(User user, RoleName role) { return user.getRoles().stream().anyMatch(r -> r.getName() == role); }
  private void syncTeacherAssignment(User student, Set<Role> userRoles, UUID teacherId) {
    var isStudent = userRoles.stream().anyMatch(r -> r.getName() == RoleName.STUDENT);
    if (!isStudent) {
      assignments.deleteByStudentId(student.getId());
      return;
    }
    if (teacherId == null) throw new IllegalArgumentException("Teacher is required for student accounts");
    var teacher = users.findById(teacherId).orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
    if (teacher.getDeletedAt() != null || !hasRole(teacher, RoleName.TEACHER)) throw new IllegalArgumentException("Selected user is not an active teacher");
    var assignment = assignments.findByStudentIdAndDeletedAtIsNull(student.getId()).orElseGet(() -> {
      var a = new StudentTeacherAssignment(); a.setStudent(student); return a;
    });
    assignment.setTeacher(teacher);
    assignments.save(assignment);
  }
  private UserDtos.UserResponse toResponse(User user) {
    var teacher = assignments.findByStudentIdAndDeletedAtIsNull(user.getId()).map(StudentTeacherAssignment::getTeacher).orElse(null);
    return new UserDtos.UserResponse(
      user.getId(),
      user.getEmail(),
      user.getFullName(),
      user.isEnabled(),
      user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
      teacher == null ? null : teacher.getId(),
      teacher == null ? null : teacher.getFullName()
    );
  }
}

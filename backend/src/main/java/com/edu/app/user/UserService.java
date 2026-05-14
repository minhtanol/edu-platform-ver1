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
  private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder encoder; private final UserMapper mapper;
  public Page<UserDtos.UserResponse> list(Pageable pageable) { return users.findByDeletedAtIsNull(pageable).map(mapper::toResponse); }
  public Page<UserDtos.UserResponse> students(Pageable pageable) { return users.findByRoles_NameAndDeletedAtIsNull(RoleName.STUDENT, pageable).map(mapper::toResponse); }
  public UserDtos.UserResponse me(String email) { return users.findByEmailAndDeletedAtIsNull(email).map(mapper::toResponse).orElseThrow(); }
  @Transactional public UserDtos.UserResponse create(UserDtos.CreateUserRequest req) {
    users.findByEmailAndDeletedAtIsNull(req.email()).ifPresent(existing -> { throw new IllegalArgumentException("Email already exists"); });
    var u = new User(); u.setEmail(req.email()); u.setFullName(req.fullName()); u.setPasswordHash(encoder.encode(req.password())); u.setRoles(resolve(req.roles())); return mapper.toResponse(users.save(u));
  }
  @Transactional public UserDtos.UserResponse update(UUID id, UserDtos.UpdateUserRequest req) {
    var u = users.findById(id).orElseThrow(); if (req.fullName()!=null) u.setFullName(req.fullName()); if (req.enabled()!=null) u.setEnabled(req.enabled()); if (req.roles()!=null) u.setRoles(resolve(req.roles())); return mapper.toResponse(u);
  }
  @Transactional public void delete(UUID id) { var u = users.findById(id).orElseThrow(); u.setDeletedAt(Instant.now()); }
  private Set<Role> resolve(Set<RoleName> names) { return Optional.ofNullable(names).orElse(Set.of(RoleName.STUDENT)).stream().map(n -> roles.findByName(n).orElseThrow()).collect(Collectors.toSet()); }
}

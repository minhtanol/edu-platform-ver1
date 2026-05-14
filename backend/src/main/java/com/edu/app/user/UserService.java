package com.edu.app.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class UserService {
  private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder encoder; private final StudentTeacherAssignmentRepository assignments; private final StudentProfileRepository profiles; private final StudentProfileHistoryRepository profileHistories;
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
    syncStudentProfile(saved, null, resolvedRoles, req.address(), req.guardianName(), req.guardianPhone(), req.hometown(), req.allergies(), req.dateOfBirth(), req.gender(), req.emergencyContactName(), req.emergencyContactPhone(), req.studyStatus());
    return toResponse(saved);
  }
  @Transactional public UserDtos.UserResponse update(UUID id, UserDtos.UpdateUserRequest req) { return update(id, req, null); }
  @Transactional public UserDtos.UserResponse update(UUID id, UserDtos.UpdateUserRequest req, String actorEmail) {
    var u = users.findById(id).orElseThrow(); var actor = actorEmail == null ? null : users.findByEmailAndDeletedAtIsNull(actorEmail).orElse(null); if (req.fullName()!=null) u.setFullName(req.fullName()); if (req.enabled()!=null) u.setEnabled(req.enabled()); if (req.roles()!=null) u.setRoles(resolve(req.roles())); if (req.roles()!=null || req.teacherId()!=null) syncTeacherAssignment(u, u.getRoles(), req.teacherId()); syncStudentProfile(u, actor, u.getRoles(), req.address(), req.guardianName(), req.guardianPhone(), req.hometown(), req.allergies(), req.dateOfBirth(), req.gender(), req.emergencyContactName(), req.emergencyContactPhone(), req.studyStatus()); return toResponse(u);
  }
  @Transactional public void delete(UUID id) { assignments.deleteByStudentId(id); profiles.deleteByUserId(id); var u = users.findById(id).orElseThrow(); u.setDeletedAt(Instant.now()); }
  public Page<UserDtos.StudentProfileHistoryResponse> profileHistory(UUID studentId, Pageable pageable) { return profileHistories.findByStudentIdAndDeletedAtIsNullOrderByCreatedAtDesc(studentId, pageable).map(this::toHistoryResponse); }
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
  private void syncStudentProfile(User user, User actor, Set<Role> userRoles, String address, String guardianName, String guardianPhone, String hometown, String allergies, LocalDate dateOfBirth, StudentProfile.Gender gender, String emergencyContactName, String emergencyContactPhone, StudentProfile.StudyStatus studyStatus) {
    var isStudent = userRoles.stream().anyMatch(r -> r.getName() == RoleName.STUDENT);
    if (!isStudent) {
      profiles.deleteByUserId(user.getId());
      return;
    }
    var profile = profiles.findByUserIdAndDeletedAtIsNull(user.getId()).orElseGet(() -> {
      var p = new StudentProfile(); p.setUser(user); return p;
    });
    var before = snapshot(profile);
    if (address != null) profile.setAddress(address);
    if (guardianName != null) profile.setGuardianName(guardianName);
    if (guardianPhone != null) profile.setGuardianPhone(guardianPhone);
    if (hometown != null) profile.setHometown(hometown);
    if (allergies != null) profile.setAllergies(allergies);
    if (dateOfBirth != null) profile.setDateOfBirth(dateOfBirth);
    if (gender != null) profile.setGender(gender);
    if (emergencyContactName != null) profile.setEmergencyContactName(emergencyContactName);
    if (emergencyContactPhone != null) profile.setEmergencyContactPhone(emergencyContactPhone);
    if (studyStatus != null) profile.setStudyStatus(studyStatus);
    profiles.save(profile);
    var after = snapshot(profile);
    if (!Objects.equals(before, after)) {
      var h = new StudentProfileHistory(); h.setStudent(user); h.setActor(actor); h.setBeforeValue(before); h.setAfterValue(after); profileHistories.save(h);
    }
  }
  private UserDtos.UserResponse toResponse(User user) {
    var teacher = assignments.findByStudentIdAndDeletedAtIsNull(user.getId()).map(StudentTeacherAssignment::getTeacher).orElse(null);
    var profile = profiles.findByUserIdAndDeletedAtIsNull(user.getId()).orElse(null);
    return new UserDtos.UserResponse(
      user.getId(),
      user.getEmail(),
      user.getFullName(),
      user.isEnabled(),
      user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
      teacher == null ? null : teacher.getId(),
      teacher == null ? null : teacher.getFullName(),
      profile == null ? null : profile.getAddress(),
      profile == null ? null : profile.getGuardianName(),
      profile == null ? null : profile.getGuardianPhone(),
      profile == null ? null : profile.getHometown(),
      profile == null ? null : profile.getAllergies(),
      profile == null ? null : profile.getDateOfBirth(),
      age(profile),
      profile == null ? null : profile.getGender(),
      profile == null ? null : profile.getEmergencyContactName(),
      profile == null ? null : profile.getEmergencyContactPhone(),
      profile == null ? null : profile.getStudyStatus()
    );
  }
  private Integer age(StudentProfile profile) { return profile == null || profile.getDateOfBirth() == null ? null : Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears(); }
  private String snapshot(StudentProfile p) {
    return "{" +
      "\"address\":\"" + safe(p.getAddress()) + "\"," +
      "\"guardianName\":\"" + safe(p.getGuardianName()) + "\"," +
      "\"guardianPhone\":\"" + safe(p.getGuardianPhone()) + "\"," +
      "\"hometown\":\"" + safe(p.getHometown()) + "\"," +
      "\"allergies\":\"" + safe(p.getAllergies()) + "\"," +
      "\"dateOfBirth\":\"" + safe(p.getDateOfBirth()) + "\"," +
      "\"gender\":\"" + safe(p.getGender()) + "\"," +
      "\"emergencyContactName\":\"" + safe(p.getEmergencyContactName()) + "\"," +
      "\"emergencyContactPhone\":\"" + safe(p.getEmergencyContactPhone()) + "\"," +
      "\"studyStatus\":\"" + safe(p.getStudyStatus()) + "\"" +
      "}";
  }
  private String safe(Object value) { return value == null ? "" : String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\""); }
  private UserDtos.StudentProfileHistoryResponse toHistoryResponse(StudentProfileHistory h) {
    return new UserDtos.StudentProfileHistoryResponse(h.getId(), h.getStudent().getId(), h.getStudent().getFullName(), h.getActor() == null ? null : h.getActor().getId(), h.getActor() == null ? null : h.getActor().getFullName(), h.getBeforeValue(), h.getAfterValue(), h.getCreatedAt());
  }
}

package com.edu.app.user;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()))")
  @Mapping(target = "teacherId", expression = "java(null)")
  @Mapping(target = "teacherName", expression = "java(null)")
  @Mapping(target = "address", expression = "java(null)")
  @Mapping(target = "guardianName", expression = "java(null)")
  @Mapping(target = "guardianPhone", expression = "java(null)")
  @Mapping(target = "hometown", expression = "java(null)")
  @Mapping(target = "allergies", expression = "java(null)")
  UserDtos.UserResponse toResponse(User user);
}

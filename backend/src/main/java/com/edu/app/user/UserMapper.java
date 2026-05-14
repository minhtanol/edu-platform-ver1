package com.edu.app.user;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()))")
  UserDtos.UserResponse toResponse(User user);
}

package com.edu.app.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity @Table(name = "roles")
@Getter @Setter
public class Role {
  @Id private UUID id;
  @Enumerated(EnumType.STRING) @Column(nullable = false, unique = true, length = 40) private RoleName name;
}

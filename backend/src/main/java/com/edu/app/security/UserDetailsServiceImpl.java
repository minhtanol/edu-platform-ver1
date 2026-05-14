package com.edu.app.security;

import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository users;
  public UserDetails loadUserByUsername(String email) {
    var u = users.findByEmailAndDeletedAtIsNull(email).orElseThrow(() -> new UsernameNotFoundException(email));
    var auths = u.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName())).toList();
    return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPasswordHash(), u.isEnabled(), true, true, true, auths);
  }
}

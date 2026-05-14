package com.edu.app.security;

import com.edu.app.user.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component @RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt; private final UserRepository users;
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    var header = req.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      try {
        var email = jwt.subject(header.substring(7));
        users.findByEmailAndDeletedAtIsNull(email).ifPresent(u -> {
          var auths = u.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName())).toList();
          SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u.getEmail(), null, auths));
        });
      } catch (Exception ignored) { }
    }
    chain.doFilter(req, res);
  }
}

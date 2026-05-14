package com.edu.app.config;

import com.edu.app.security.JwtAuthFilter;
import com.edu.app.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration @EnableMethodSecurity @RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthFilter jwtAuthFilter; private final RateLimitFilter rateLimitFilter;
  @Value("${app.cors-origins}") private String corsOrigins;
  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
  @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception { return cfg.getAuthenticationManager(); }
  @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable()).cors(c -> c.configurationSource(corsConfigurationSource()))
      .headers(h -> h.frameOptions(f -> f.sameOrigin()).contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'")))
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a
        .requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/actuator/health").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/media/stream/**").authenticated()
        .anyRequest().authenticated())
      .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class).build();
  }
  @Bean CorsConfigurationSource corsConfigurationSource() {
    var cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of(corsOrigins.split(","))); cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*")); cfg.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", cfg); return source;
  }
}

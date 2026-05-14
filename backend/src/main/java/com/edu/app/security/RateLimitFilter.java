package com.edu.app.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private record Window(long epochMinute, AtomicInteger count) {}
  private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    var key = req.getRemoteAddr() + ":" + req.getRequestURI();
    var minute = Instant.now().getEpochSecond() / 60;
    var window = buckets.compute(key, (k, old) -> old == null || old.epochMinute != minute ? new Window(minute, new AtomicInteger()) : old);
    if (window.count.incrementAndGet() > 120) {
      res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      res.setContentType("application/json");
      res.getWriter().write("{\"success\":false,\"message\":\"Too many requests\"}");
      return;
    }
    chain.doFilter(req, res);
  }
}

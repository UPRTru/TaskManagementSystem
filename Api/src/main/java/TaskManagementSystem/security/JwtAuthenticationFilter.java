package TaskManagementSystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import TaskManagementSystem.controller.Controller;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil = new JwtUtil();
  private static final Logger log = LoggerFactory.getLogger(Controller.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String token = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (token != null && token.startsWith("Bearer ") && jwtUtil.isTokenExpired(token = token.substring(7))) {
      Authentication authentication = createAuthentication(token);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.info("Authentication token: {}", token);
    }
    filterChain.doFilter(request, response);
  }

  //_______________________________________
  private Authentication createAuthentication(String token) {
    // Получение информации о пользователе из токена
    UserDetails userDetails = jwtUtil.getUserDetailsFromToken(token);

    // Создание объекта Authentication
    return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
  }
}


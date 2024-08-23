package TaskManagementSystem.security;

import TaskManagementSystem.controller.Controller;
import TaskManagementSystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
  private UserService userDetailsService;
  private JwtUtil jwtUtil;
  private static final Logger log = LoggerFactory.getLogger(Controller.class);

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    // Логика аутентификации с использованием токена
    String token = (String) authentication.getCredentials();
    try {
      String username = jwtUtil.getUsernameFromToken(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      log.info("UsernamePasswordAuthentication User: {}", username);
      try {
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
      } catch (Exception e) {
        userDetailsService.lockedUserEmail(username);
        throw new BadCredentialsException(e.getMessage());
      }
    } catch (Exception e) {
      log.info("JwtAuthenticationProvider authenticate() {}", e.getMessage());
      throw new BadCredentialsException(e.getMessage());
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}

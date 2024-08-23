package TaskManagementSystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
  private final String secret = "984hg493gh0439rthr0429uruj2309yh937gc763fe87t3f89723gf";
  private final Duration lifetime = Duration.ofHours(1);

  public String generateToken(UserDetails userDetails) {
    // Логика генерации JWT
    Map<String, Object> claims = new HashMap<>();
    String email = userDetails.getUsername();
    claims.put("email", email);
    Date now = new Date();
    Date expiration = new Date(now.getTime() + lifetime.toMillis());
    return Jwts.builder()
        .claims(claims)
        .subject(userDetails.getUsername())
        .issuedAt(now)
        .expiration(expiration)
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return getAllClaimsFromToken(token).getSubject();
  }

  public Claims getAllClaimsFromToken(String token) {
    return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean isTokenExpired(String token) {
    Date expiration = getAllClaimsFromToken(token).getExpiration();
    return expiration.after(new Date());
  }

  public UserDetails getUserDetailsFromToken(String token) {
    return new User(getUsernameFromToken(token), "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }
}


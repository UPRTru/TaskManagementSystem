package TaskManagementSystem.service;

import TaskManagementSystem.controller.Controller;
import TaskManagementSystem.dto.TokenDto;
import TaskManagementSystem.dto.UserDto;
import TaskManagementSystem.exception.AlreadyExistsException;
import TaskManagementSystem.exception.IncorrectDataException;
import TaskManagementSystem.model.User;
import TaskManagementSystem.repository.UserRepository;
import TaskManagementSystem.security.JwtUtil;
import TaskManagementSystem.security.SecurityConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
  private UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private static final Logger log = LoggerFactory.getLogger(Controller.class);
  private SecurityConfig securityConfig;

  @Autowired
  public UserService(UserRepository userRepository, JwtUtil jwtUtil,
      SecurityConfig securityConfig) {
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
    this.securityConfig = securityConfig;
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public void createNewUser(UserDto userDto) {
    try {
      validUser(userDto);
      if (findByEmail(userDto.getEmail()).isPresent()) {
        throw new AlreadyExistsException("Пользователь " + userDto.getEmail() + " уже существует");
      }
      User user = new User();
      user.setEmail(userDto.getEmail());
      user.setPassword(securityConfig.passwordEncoder().encode(userDto.getPassword()));
      userRepository.save(user);
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  public void deleteUser(String email) {
    User user = findByEmail(email).get();
    userRepository.delete(user);
  }

  public void lockedUserEmail(String email) {
    User user = findByEmail(email).get();
    if (user.getLocked() > 0) {
      user.setLocked(user.getLocked() - 1);
      userRepository.save(user);
    }
  }

  public ResponseEntity<?> authenticateUser(UserDto userDto) {
    try {
      validUser(userDto);
      UserDetails userDetails = loadUserByUsername(userDto.getEmail());
      if (!securityConfig.passwordEncoder().matches(userDto.getPassword(), userDetails.getPassword())) {
        log.info("Неверный пароль. User: {}", userDto.getEmail());
        lockedUserEmail(userDetails.getUsername());
        throw new BadCredentialsException("Неверный пароль");
      }
      String token = jwtUtil.generateToken(userDetails);
      return ResponseEntity.ok(new TokenDto(token));
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    try {
      User user = findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Пользователь " + email + " не найден"));
      if (user.getLocked() <= 0) {
        throw new BadCredentialsException("Пользователь " + email + " заблокирован");
      }
      return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  private void validUser(UserDto userDto) {
    try {
      String email = userDto.getEmail();
      Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
      Matcher mat = pattern.matcher(email);
      if (userDto.getEmail().isEmpty() || !mat.matches()) {
        throw new IncorrectDataException("Некорректная почта " + email);
      } else
        if (userDto.getPassword().isEmpty() || userDto.getPassword() == "") {
          throw new IncorrectDataException("Пароль не может быть пустым");
        }
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }
}
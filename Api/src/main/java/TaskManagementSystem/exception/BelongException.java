package TaskManagementSystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class BelongException extends RuntimeException {
  public BelongException(String message) {
    super(message);
  }
}

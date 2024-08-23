package TaskManagementSystem.exception;

import TaskManagementSystem.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    @ExceptionHandler(value = AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.ALREADY_REPORTED)
    @ResponseBody
    public String handleAlreadyExistsException(AlreadyExistsException e) {
        return logException(e);
    }

    @ExceptionHandler(value = IncorrectDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleIncorrectDataException(IncorrectDataException e) {
        return logException(e);
    }

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleNotFoundException(NotFoundException e) {
        return logException(e);
    }

    @ExceptionHandler(value = BelongException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String handleBelongException(BelongException e) {
        return logException(e);
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String handleUsernameNotFoundException(UsernameNotFoundException e) {
        return logException(e);
    }

    private String logException(Exception e) {
        log.error("ResponseStatus: {}", e.getMessage());
        return "Ошибка: " + e.getMessage();
    }
}

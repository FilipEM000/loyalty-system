package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long id) {
        super("User with id [" + id + "] not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}

package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String email) {
        super("User with email [" + email + "] already exists", "EMAIL_TAKEN", HttpStatus.CONFLICT);
    }
}

package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class InsufficientPointsException extends BusinessException {
    public InsufficientPointsException(String message) {
        super(message, "INSUFFICIENT_POINTS", HttpStatus.CONFLICT);
    }
}

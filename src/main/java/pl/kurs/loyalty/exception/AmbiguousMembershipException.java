package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class AmbiguousMembershipException extends BusinessException {
    public AmbiguousMembershipException(String message) {
        super(message, "AMBIGUOUS_MEMBERSHIP", HttpStatus.BAD_REQUEST);
    }
}

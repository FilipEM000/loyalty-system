package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class MembershipAlreadyExistsException extends BusinessException {
    public MembershipAlreadyExistsException(String message) {
        super(message, "MEMBERSHIP_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }
}

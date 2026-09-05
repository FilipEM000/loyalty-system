package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class MembershipAlreadyExistsException extends BusinessException {
    public MembershipAlreadyExistsException() {
        super("Membership already exists", "MEMBERSHIP_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }
}

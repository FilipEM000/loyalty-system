package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class MembershipNotFoundException extends BusinessException {
    public MembershipNotFoundException() {
        super("Membership not found", "MEMBERSHIP_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}

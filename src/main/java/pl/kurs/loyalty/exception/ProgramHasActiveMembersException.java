package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class ProgramHasActiveMembersException extends BusinessException {
    public ProgramHasActiveMembersException() {
        super("Cannot delete program with active members", "ACTIVE_MEMBERS", HttpStatus.CONFLICT);
    }
}

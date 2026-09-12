package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class ProgramExpiredException extends BusinessException {
    public ProgramExpiredException() {
        super("Loyalty program expired", "PROGRAM_EXPIRED", HttpStatus.CONFLICT);
    }
}

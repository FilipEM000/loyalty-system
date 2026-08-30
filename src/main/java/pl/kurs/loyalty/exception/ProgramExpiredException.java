package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class ProgramExpiredException extends BusinessException {
    public ProgramExpiredException(String message) {
        super(message, "PROGRAM_EXPIRED", HttpStatus.CONFLICT);
    }
}

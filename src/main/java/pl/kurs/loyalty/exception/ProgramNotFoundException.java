package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class ProgramNotFoundException extends BusinessException {
    public ProgramNotFoundException(String message) {
        super(message, "PROGRAM_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}

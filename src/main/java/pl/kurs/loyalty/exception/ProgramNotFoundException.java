package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class ProgramNotFoundException extends BusinessException {
    public ProgramNotFoundException(Long id) {
        super("Loyalty program with id [" + id + "] was not found", "PROGRAM_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}

package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class ProgramAlreadyExistsException extends BusinessException {
    public ProgramAlreadyExistsException(String name) {
        super("Loyalty program with name [" + name + "] already exists", "NAME_TAKEN", HttpStatus.CONFLICT);
    }
}

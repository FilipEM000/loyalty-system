package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class PositiveBalanceException extends BusinessException {
    public PositiveBalanceException() {
        super("Cannot quit membership with positive balance", "POSITIVE_BALANCE_ERROR", HttpStatus.CONFLICT);
    }
}

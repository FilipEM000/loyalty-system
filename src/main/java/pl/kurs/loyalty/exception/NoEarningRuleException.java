package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class NoEarningRuleException extends BusinessException {
    public NoEarningRuleException(String message) {
        super(message, "NO_EARNING_RULE", HttpStatus.CONFLICT);
    }
}

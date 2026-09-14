package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class NoEarningRuleException extends BusinessException {
    public NoEarningRuleException() {
        super("No active rule for this event", "NO_EARNING_RULE", HttpStatus.CONFLICT);
    }
}

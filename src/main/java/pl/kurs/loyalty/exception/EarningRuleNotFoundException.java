package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class EarningRuleNotFoundException extends BusinessException {
    public EarningRuleNotFoundException(Long id) {
        super("Earning rile with id [" + id + "] was not found", "RULE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}

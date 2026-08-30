package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class RewardUnavailableException extends BusinessException {
    public RewardUnavailableException(String message) {
        super(message, "REWARD_UNAVAILABLE", HttpStatus.CONFLICT);
    }
}

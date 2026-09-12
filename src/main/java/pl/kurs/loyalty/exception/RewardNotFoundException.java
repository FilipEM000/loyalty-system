package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class RewardNotFoundException extends BusinessException {
    public RewardNotFoundException(Long id) {
        super("Reward with id [" + id + "] was not found", "REWARD_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}

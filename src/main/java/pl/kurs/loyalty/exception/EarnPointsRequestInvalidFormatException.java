package pl.kurs.loyalty.exception;

import org.springframework.http.HttpStatus;

public class EarnPointsRequestInvalidFormatException extends BusinessException {
    public EarnPointsRequestInvalidFormatException() {
        super("You have to specify one of the approved formats for earning points", "INVALID_EARN_POINT_FORMAT", HttpStatus.BAD_REQUEST);
    }
}

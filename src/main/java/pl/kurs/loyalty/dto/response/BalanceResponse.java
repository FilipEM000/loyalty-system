package pl.kurs.loyalty.dto.response;

import java.time.LocalDateTime;

public record BalanceResponse(
        Long programId,
        String programName,
        Integer pointsBalance,
        LocalDateTime joinTime
) {
}

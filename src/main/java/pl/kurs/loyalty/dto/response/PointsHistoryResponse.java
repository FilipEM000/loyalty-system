package pl.kurs.loyalty.dto.response;

import pl.kurs.loyalty.model.TransactionType;

import java.time.LocalDateTime;

public record PointsHistoryResponse(
        String programName,
        LocalDateTime date,
        Integer points,
        TransactionType type,
        String description
) {
}

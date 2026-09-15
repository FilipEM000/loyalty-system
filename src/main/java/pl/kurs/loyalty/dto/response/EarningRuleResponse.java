package pl.kurs.loyalty.dto.response;

import java.time.LocalDateTime;

public record EarningRuleResponse(
        Long id,
        String name,
        String eventType,
        Integer numberOfPoints,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean isActive
) {
}

package pl.kurs.loyalty.dto.response;

import java.time.LocalDateTime;

public record LoyaltyProgramResponse(
        Long id,
        String name,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean isActive
) {
}

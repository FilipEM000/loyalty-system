package pl.kurs.loyalty.dto.response;

import java.time.LocalDateTime;

public record RewardResponse(
        Long id,
        String name,
        String description,
        Integer cost,
        Integer availableQuantity,
        LocalDateTime startDate,
        LocalDateTime  endDate
) {
}

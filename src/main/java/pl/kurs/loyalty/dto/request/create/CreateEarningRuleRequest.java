package pl.kurs.loyalty.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import pl.kurs.loyalty.model.EarningEventType;

import java.time.LocalDateTime;

public record CreateEarningRuleRequest(
        @NotBlank String name,
        @NotNull EarningEventType eventType,
        @NotNull Integer numberOfPoints,
        @NotNull LocalDateTime startDate,
        @Nullable LocalDateTime endDate
) {
}

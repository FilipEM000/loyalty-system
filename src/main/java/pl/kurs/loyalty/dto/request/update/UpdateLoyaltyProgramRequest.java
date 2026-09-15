package pl.kurs.loyalty.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record UpdateLoyaltyProgramRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull LocalDateTime startDate,
        @Nullable LocalDateTime endDate
) {
}

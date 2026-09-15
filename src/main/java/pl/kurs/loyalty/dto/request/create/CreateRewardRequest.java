package pl.kurs.loyalty.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record CreateRewardRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull Integer cost,
        @Nullable Integer availableQuantity,
        @NotNull LocalDateTime startDate,
        @Nullable LocalDateTime endDate
) {
}

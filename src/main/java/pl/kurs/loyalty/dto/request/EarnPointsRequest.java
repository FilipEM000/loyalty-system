package pl.kurs.loyalty.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import pl.kurs.loyalty.model.EarningEventType;

public record EarnPointsRequest(
        @Nullable EarningEventType earningEventType,
        @Nullable Long programId,
        @Nullable Long earningRuleId,
        @NotBlank String referenceId
) {
}

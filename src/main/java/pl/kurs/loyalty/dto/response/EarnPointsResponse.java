package pl.kurs.loyalty.dto.response;

public record EarnPointsResponse(
        Long id,
        Integer points,
        Integer newBalance,
        String description
) {
}

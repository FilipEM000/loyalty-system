package pl.kurs.loyalty.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String lastName,
        String email,
        LocalDateTime registrationDate,
        List<ProgramSummaryResponse> programs
) {
}

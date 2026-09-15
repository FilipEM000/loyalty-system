package pl.kurs.loyalty.dto.request.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String name,
        @NotBlank String lastName,
        @NotBlank @Email String email
) {
}

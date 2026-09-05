package pl.kurs.loyalty.dto.request;

public record UpdateUserRequest(
        String name,
        String lastName,
        String email
) {
}

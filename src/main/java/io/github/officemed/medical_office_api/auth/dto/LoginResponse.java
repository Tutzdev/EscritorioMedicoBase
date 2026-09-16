package io.github.officemed.medical_office_api.auth.dto;

public record LoginResponse(
        TokenResponse tokens,
        CurrentUserResponse user
) {
}

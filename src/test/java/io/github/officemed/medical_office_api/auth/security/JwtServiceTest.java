package io.github.officemed.medical_office_api.auth.security;

import io.github.officemed.medical_office_api.auth.exception.InvalidRefreshTokenException;
import io.github.officemed.medical_office_api.user.entity.Role;
import io.github.officemed.medical_office_api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                new ObjectMapper(),
                "test-secret-with-at-least-thirty-two-characters",
                Duration.ofMinutes(15),
                Duration.ofDays(7),
                "medical-office-test"
        );

        userId = UUID.randomUUID();
        user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getRole()).thenReturn(Role.ADMIN);
    }

    @Test
    void shouldCreateAndReadAccessToken() {
        String token = jwtService.createAccessToken(user);

        assertThat(jwtService.extractAccessTokenUserId(token)).isEqualTo(userId);
        assertThat(jwtService.getAccessTokenExpirationSeconds()).isEqualTo(900);
    }

    @Test
    void shouldNotAcceptRefreshTokenAsAccessToken() {
        String refreshToken = jwtService.createRefreshToken(user);

        assertThatThrownBy(() -> jwtService.extractAccessTokenUserId(refreshToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldTranslateMalformedRefreshTokenToDomainException() {
        assertThatThrownBy(() -> jwtService.extractRefreshTokenUserId("invalid"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}

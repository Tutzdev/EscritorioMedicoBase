package io.github.officemed.medical_office_api.auth.service;

import io.github.officemed.medical_office_api.auth.dto.CurrentUserResponse;
import io.github.officemed.medical_office_api.auth.dto.LoginRequest;
import io.github.officemed.medical_office_api.auth.dto.LoginResponse;
import io.github.officemed.medical_office_api.auth.dto.RefreshTokenRequest;
import io.github.officemed.medical_office_api.auth.dto.TokenResponse;
import io.github.officemed.medical_office_api.auth.exception.InvalidCredentialsException;
import io.github.officemed.medical_office_api.auth.exception.InvalidRefreshTokenException;
import io.github.officemed.medical_office_api.auth.security.JwtService;
import io.github.officemed.medical_office_api.auth.security.UserPrincipal;
import io.github.officemed.medical_office_api.user.entity.User;
import io.github.officemed.medical_office_api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmailIgnoreCase(request.email())
                .filter(User::isActive)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(
                createTokens(user),
                toCurrentUser(user)
        );
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshTokenRequest request) {
        UUID userId = jwtService.extractRefreshTokenUserId(request.refreshToken());

        User user = userRepository
                .findById(userId)
                .filter(User::isActive)
                .orElseThrow(InvalidRefreshTokenException::new);

        return createTokens(user);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(UserPrincipal principal) {
        User user = userRepository
                .findById(principal.getId())
                .filter(User::isActive)
                .orElseThrow(InvalidCredentialsException::new);

        return toCurrentUser(user);
    }

    private TokenResponse createTokens(User user) {
        return new TokenResponse(
                jwtService.createAccessToken(user),
                jwtService.createRefreshToken(user),
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds()
        );
    }

    private CurrentUserResponse toCurrentUser(User user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}

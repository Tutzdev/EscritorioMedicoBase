package io.github.officemed.medical_office_api.auth.controller;

import io.github.officemed.medical_office_api.auth.dto.CurrentUserResponse;
import io.github.officemed.medical_office_api.auth.dto.LoginRequest;
import io.github.officemed.medical_office_api.auth.dto.LoginResponse;
import io.github.officemed.medical_office_api.auth.dto.RefreshTokenRequest;
import io.github.officemed.medical_office_api.auth.dto.TokenResponse;
import io.github.officemed.medical_office_api.auth.security.UserPrincipal;
import io.github.officemed.medical_office_api.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> currentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(authService.currentUser(principal));
    }
}

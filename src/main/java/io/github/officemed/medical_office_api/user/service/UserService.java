package io.github.officemed.medical_office_api.user.service;

import io.github.officemed.medical_office_api.user.dto.CreateUserRequest;
import io.github.officemed.medical_office_api.user.dto.UserResponse;
import io.github.officemed.medical_office_api.user.entity.User;
import io.github.officemed.medical_office_api.user.exception.EmailAlreadyInUseException;
import io.github.officemed.medical_office_api.user.mapper.UserMapper;
import io.github.officemed.medical_office_api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        validateEmailAvailability(normalizedEmail);

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.create(
                request.name(),
                normalizedEmail,
                passwordHash,
                request.role()
        );

        return UserMapper.toResponse(userRepository.save(user));
    }

    private void validateEmailAvailability(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyInUseException(email);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
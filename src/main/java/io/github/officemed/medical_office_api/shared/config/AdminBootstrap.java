package io.github.officemed.medical_office_api.shared.config;

import io.github.officemed.medical_office_api.user.entity.Role;
import io.github.officemed.medical_office_api.user.entity.User;
import io.github.officemed.medical_office_api.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.bootstrap.admin.enabled",
        havingValue = "true"
)
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String name;
    private final String email;
    private final String password;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.name}") String name,
            @Value("${app.bootstrap.admin.email}") String email,
            @Value("${app.bootstrap.admin.password}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        validateConfiguration();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        User administrator = User.create(
                name,
                email,
                passwordEncoder.encode(password),
                Role.ADMIN
        );

        userRepository.save(administrator);
    }

    private void validateConfiguration() {
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 12) {
            throw new IllegalStateException(
                    "Administrator bootstrap requires name, email and a password with at least 12 characters"
            );
        }
    }
}

package io.github.officemed.medical_office_api.auth.service;

import io.github.officemed.medical_office_api.auth.security.UserPrincipal;
import io.github.officemed.medical_office_api.user.entity.User;
import io.github.officemed.medical_office_api.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return UserPrincipal.from(user);
    }

    public UserPrincipal loadUserById(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return UserPrincipal.from(user);
    }
}

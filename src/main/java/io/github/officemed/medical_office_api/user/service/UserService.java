package io.github.officemed.medical_office_api.user.service;

import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import io.github.officemed.medical_office_api.user.dto.ChangePasswordRequest;
import io.github.officemed.medical_office_api.user.dto.CreateUserRequest;
import io.github.officemed.medical_office_api.user.dto.UpdateUserRequest;
import io.github.officemed.medical_office_api.user.dto.UpdateUserStatusRequest;
import io.github.officemed.medical_office_api.user.dto.UserResponse;
import io.github.officemed.medical_office_api.user.entity.Role;
import io.github.officemed.medical_office_api.user.entity.User;
import io.github.officemed.medical_office_api.user.exception.EmailAlreadyInUseException;
import io.github.officemed.medical_office_api.user.exception.UserNotFoundException;
import io.github.officemed.medical_office_api.user.mapper.UserMapper;
import io.github.officemed.medical_office_api.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyInUseException(normalizedEmail);
        }

        User user = User.create(
                request.name(),
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.role()
        );

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(
            String search,
            Role role,
            Boolean active,
            Pageable pageable
    ) {
        Page<UserResponse> users = userRepository
                .findAll(buildSpecification(search, role, active), limit(pageable))
                .map(userMapper::toResponse);

        return PageResponse.from(users);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID userId) {
        return userMapper.toResponse(findEntityById(userId));
    }

    @Transactional
    public UserResponse update(
            UUID userId,
            UpdateUserRequest request
    ) {
        User user = findEntityById(userId);

        user.updateProfile(
                request.name(),
                request.role()
        );

        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(
            UUID userId,
            ChangePasswordRequest request
    ) {
        User user = findEntityById(userId);
        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public UserResponse updateStatus(
            UUID userId,
            UpdateUserStatusRequest request
    ) {
        User user = findEntityById(userId);

        if (request.active()) {
            user.activate();
        } else {
            user.deactivate();
        }

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public User findEntityById(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Specification<User> buildSpecification(
            String search,
            Role role,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), term)
                ));
            }

            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Pageable limit(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                pageable.getSort()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

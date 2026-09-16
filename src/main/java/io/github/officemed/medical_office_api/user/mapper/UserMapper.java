package io.github.officemed.medical_office_api.user.mapper;

import io.github.officemed.medical_office_api.user.dto.UserResponse;
import io.github.officemed.medical_office_api.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

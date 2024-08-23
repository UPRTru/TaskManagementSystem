package TaskManagementSystem.mapper;

import TaskManagementSystem.dto.UserDto;
import TaskManagementSystem.model.User;

public class UserMapper {
    static UserDto userToDto(User user) {
        if (user == null) {
            return null;
        } else {
            return UserDto.builder()
                    .email(user.getEmail())
                    .password(null)
                    .build();
        }
    }
}

package pablo.tzeliks.blink_link.application.user.mapper;

import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.user.dto.UserResponse;
import pablo.tzeliks.blink_link.domain.user.model.User;

@Component
public class UserDtoMapper {

    public UserResponse toDto(User domain) {

        return new UserResponse(
                domain.getId().toString(),
                domain.getEmail().getValue(),
                domain.getRole().toString(),
                domain.getPlan().toString(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}

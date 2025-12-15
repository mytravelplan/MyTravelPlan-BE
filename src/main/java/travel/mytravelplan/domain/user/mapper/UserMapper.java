package travel.mytravelplan.domain.user.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.user.dto.UserDto;
import travel.mytravelplan.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}

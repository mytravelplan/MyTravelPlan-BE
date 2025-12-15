package travel.mytravelplan.domain.user.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.user.dto.FollowDto;
import travel.mytravelplan.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface FollowMapper {
    FollowDto toDto(User user, boolean isFollowing);
}

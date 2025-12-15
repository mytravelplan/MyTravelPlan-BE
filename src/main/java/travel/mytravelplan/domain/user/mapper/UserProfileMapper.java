package travel.mytravelplan.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.post.repository.PostRepository;
import travel.mytravelplan.domain.user.dto.UserProfileDto;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.repository.FollowRepository;

@Mapper(componentModel = "spring")
public abstract class UserProfileMapper {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRepository followRepository;

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "postCount", expression = "java(resolvePostCount(user))")
    @Mapping(target = "followerCount", expression = "java(resolveFollowerCount(user))")
    @Mapping(target = "followingCount", expression = "java(resolveFollowingCount(user))")
    abstract public UserProfileDto toDto(User user, boolean isFollowing);

    protected Long resolvePostCount(User user) {
        return postRepository.countByUser(user);
    }

    protected Long resolveFollowerCount(User user) {
        return followRepository.countByFollowingId(user.getId());
    }

    protected Long resolveFollowingCount(User user) {
        return followRepository.countByFollowerId(user.getId());
    }
}

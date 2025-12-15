package travel.mytravelplan.domain.post.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.post.dto.PostLikeDto;
import travel.mytravelplan.domain.post.entity.PostLike;

@Mapper(componentModel = "spring")
public interface PostLikeMapper {
    @Mapping(target = "postId", source = "postLike.post.id")
    @Mapping(target = "userId", source = "postLike.user.id")
    @Mapping(target = "liked", source = "liked")
    PostLikeDto toDto(PostLike postLike, boolean liked);
}

package travel.mytravelplan.domain.post.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.post.dto.PostBookMarkDto;
import travel.mytravelplan.domain.post.dto.PostLikeDto;
import travel.mytravelplan.domain.post.entity.PostBookMark;
import travel.mytravelplan.domain.post.entity.PostLike;

@Mapper(componentModel = "spring")
public interface PostBookMarkMapper {
    @Mapping(target = "postId", source = "postBookMark.post.id")
    @Mapping(target = "userId", source = "postBookMark.user.id")
    @Mapping(target = "bookmarked", source = "bookmarked")
    PostBookMarkDto toDto(PostBookMark postBookMark, boolean bookmarked);
}

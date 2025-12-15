package travel.mytravelplan.domain.comment.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.comment.dto.PostCommentDto;
import travel.mytravelplan.domain.comment.entity.PostComment;

@Mapper(componentModel = "spring")
public interface PostCommentMapper {
    PostCommentDto toDto(PostComment postComment);
}

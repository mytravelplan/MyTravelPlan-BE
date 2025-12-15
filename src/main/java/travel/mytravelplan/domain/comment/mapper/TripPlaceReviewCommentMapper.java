package travel.mytravelplan.domain.comment.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentDto;
import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;

@Mapper(componentModel = "spring")
public interface TripPlaceReviewCommentMapper {
    TripPlaceReviewCommentDto toDto(TripPlaceReviewComment tripPlaceReviewComment);
}

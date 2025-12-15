package travel.mytravelplan.domain.review.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.review.dto.TripPlaceReviewLikeDto;
import travel.mytravelplan.domain.review.entity.TripPlaceReviewLike;

@Mapper(componentModel = "spring")
public interface TripPlaceReviewLikeMapper {
    @Mapping(target = "tripPlaceReviewId", source = "tripPlaceReviewLike.tripPlaceReview.id")
    @Mapping(target = "userId", source = "tripPlaceReviewLike.user.id")
    @Mapping(target = "liked", source = "liked")
    TripPlaceReviewLikeDto toDto(TripPlaceReviewLike tripPlaceReviewLike, boolean liked);
}

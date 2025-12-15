package travel.mytravelplan.domain.review.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.review.dto.TripPlaceReviewDto;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface TripPlaceReviewMapper {
    @Mapping(target = "id", source = "tripPlaceReview.id")
    @Mapping(target = "userId", source = "tripPlaceReview.user.id")
    TripPlaceReviewDto toDto(TripPlaceReview tripPlaceReview, User user);
}

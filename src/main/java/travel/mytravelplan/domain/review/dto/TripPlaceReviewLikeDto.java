package travel.mytravelplan.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TripPlaceReviewLikeDto {
    private Long tripPlaceReviewId;
    private Long userId;
    private boolean liked;

    @Builder
    private TripPlaceReviewLikeDto(Long tripPlaceReviewId, Long userId, boolean liked) {
        this.tripPlaceReviewId = tripPlaceReviewId;
        this.userId = userId;
        this.liked = liked;
    }
}

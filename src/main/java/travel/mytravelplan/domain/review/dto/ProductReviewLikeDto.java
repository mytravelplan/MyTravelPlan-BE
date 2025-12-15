package travel.mytravelplan.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductReviewLikeDto {
    private Long reviewId;
    private Long userId;
    private boolean liked;

    @Builder
    private ProductReviewLikeDto(Long reviewId, Long userId, boolean liked) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.liked = liked;
    }
}

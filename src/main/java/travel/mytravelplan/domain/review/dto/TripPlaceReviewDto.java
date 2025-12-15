package travel.mytravelplan.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TripPlaceReviewDto {
    private Long id;
    private Long placeId;
    private Long userId;
    private String username;
    private String content;
    private BigDecimal rating;
    private boolean liked;
    private Long numberOfLikes;
    private Long numberOfComments;

    @Builder
    private TripPlaceReviewDto(Long id, Long placeId, Long userId, String username, String content,
                              BigDecimal rating, boolean liked, Long numberOfLikes, Long numberOfComments) {
        this.id = id;
        this.placeId = placeId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.rating = rating;
        this.liked = liked;
        this.numberOfLikes = numberOfLikes;
        this.numberOfComments = numberOfComments;
    }
}

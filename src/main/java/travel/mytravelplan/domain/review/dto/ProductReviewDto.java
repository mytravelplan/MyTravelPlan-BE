package travel.mytravelplan.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductReviewDto {
    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private String content;
    private BigDecimal rating;
    private boolean liked;
    private int numberOfLikes;
    private int numberOfComments;

    @Builder
    private ProductReviewDto(Long id, Long productId, Long userId, String username, String content, BigDecimal rating, boolean liked, int numberOfLikes, int numberOfComments) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.rating = rating;
        this.liked = liked;
        this.numberOfLikes = numberOfLikes;
        this.numberOfComments = numberOfComments;
    }
}

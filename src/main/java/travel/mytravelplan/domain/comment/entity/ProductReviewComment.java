package travel.mytravelplan.domain.comment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductReviewComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ProductReview productReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductReviewComment(String content, ProductReview productReview, User user) {
        this.content = content;
        this.productReview = productReview;
        this.user = user;
    }

    public static ProductReviewComment createProductReviewComment(String content, ProductReview productReview, User user) {
        return ProductReviewComment.builder()
                .content(content)
                .productReview(productReview)
                .user(user)
                .build();
    }

    public void update(String content) {
        this.content = content;
    }
}
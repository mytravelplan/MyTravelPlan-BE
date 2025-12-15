package travel.mytravelplan.domain.comment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPlaceReviewComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private TripPlaceReview tripPlaceReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private TripPlaceReviewComment(String content, TripPlaceReview tripPlaceReview, User user) {
        this.content = content;
        this.tripPlaceReview = tripPlaceReview;
        this.user = user;
    }

    public static TripPlaceReviewComment createTripPlaceReviewComment(String content, TripPlaceReview tripPlaceReview, User user) {
        return TripPlaceReviewComment.builder()
                .content(content)
                .tripPlaceReview(tripPlaceReview)
                .user(user)
                .build();
    }

    public void update(String content) {
        this.content = content;
    }
}
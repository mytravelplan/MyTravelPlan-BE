package travel.mytravelplan.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPlaceReviewLike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private TripPlaceReview tripPlaceReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private TripPlaceReviewLike(TripPlaceReview tripPlaceReview, User user) {
        this.tripPlaceReview = tripPlaceReview;
        this.user = user;
    }

    public static TripPlaceReviewLike createTripPlaceReviewLike(TripPlaceReview tripPlaceReview, User user) {
        return TripPlaceReviewLike.builder()
                .tripPlaceReview(tripPlaceReview)
                .user(user)
                .build();
    }
}

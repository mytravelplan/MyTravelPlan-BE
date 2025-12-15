package travel.mytravelplan.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.review.entity.TripPlaceReviewLike;
import travel.mytravelplan.domain.user.entity.User;

import java.util.Optional;

public interface TripPlaceReviewLikeRepository extends JpaRepository<TripPlaceReviewLike, Long> {
    Optional<TripPlaceReviewLike> findByTripPlaceReviewAndUser(TripPlaceReview tripPlaceReview, User user);
}

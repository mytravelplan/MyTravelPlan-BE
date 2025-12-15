package travel.mytravelplan.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;

public interface TripPlaceReviewRepository extends JpaRepository<TripPlaceReview, Long>, TripPlaceReviewRepositoryCustom {
}

package travel.mytravelplan.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;

public interface TripPlaceReviewCommentRepository extends JpaRepository<TripPlaceReviewComment, Long>, TripPlaceReviewCommentCustom {
}

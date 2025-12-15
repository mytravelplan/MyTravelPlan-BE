package travel.mytravelplan.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.review.entity.ProductReviewLike;
import travel.mytravelplan.domain.user.entity.User;

import java.util.Optional;

public interface ProductReviewLikeRepository extends JpaRepository<ProductReviewLike, Long> {
    Optional<ProductReviewLike> findByProductReviewAndUser(ProductReview productReview, User user);
}

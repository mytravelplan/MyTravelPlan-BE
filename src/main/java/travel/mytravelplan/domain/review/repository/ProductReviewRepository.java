package travel.mytravelplan.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.review.entity.ProductReview;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long>, ProductReviewRepositoryCustom {
}

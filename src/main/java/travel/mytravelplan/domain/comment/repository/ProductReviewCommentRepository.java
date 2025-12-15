package travel.mytravelplan.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.comment.entity.ProductReviewComment;

public interface ProductReviewCommentRepository extends JpaRepository<ProductReviewComment, Long>, ProductReviewCommentCustom {
}

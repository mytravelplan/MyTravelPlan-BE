package travel.mytravelplan.domain.review.repository;

import travel.mytravelplan.domain.review.entity.ProductReview;

import java.math.BigDecimal;
import java.util.List;

public interface ProductReviewRepositoryCustom {
    List<ProductReview> findAllByCursor(Long productId, String keyword, Boolean imgOnly, BigDecimal rating, String orderBy, String direction, String cursor, Long after, int limit);
}

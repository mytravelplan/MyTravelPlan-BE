package travel.mytravelplan.domain.comment.repository;

import travel.mytravelplan.domain.comment.entity.ProductReviewComment;

import java.util.List;

public interface ProductReviewCommentCustom {
    List<ProductReviewComment> findAllByCursor(Long productReviewId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}

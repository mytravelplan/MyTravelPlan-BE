package travel.mytravelplan.domain.review.repository;

import travel.mytravelplan.domain.review.entity.TripPlaceReview;

import java.math.BigDecimal;
import java.util.List;

public interface TripPlaceReviewRepositoryCustom {
    List<TripPlaceReview> findAllByCursor(Long tripPlaceId, String keyword, Boolean imgOnly, BigDecimal rating, String orderBy, String direction, String cursor, Long after, int limit);
}

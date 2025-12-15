package travel.mytravelplan.domain.comment.repository;

import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;

import java.util.List;

public interface TripPlaceReviewCommentCustom {
    List<TripPlaceReviewComment> findAllByCursor(Long tripPlaceReviewId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}

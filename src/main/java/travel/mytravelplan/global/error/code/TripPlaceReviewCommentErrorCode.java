package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripPlaceReviewCommentErrorCode implements ErrorCode {
    TRIP_PLACE_REVIEW_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TRIP_PLACE_REVIEW_COMMENT-01", "리뷰 댓글을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

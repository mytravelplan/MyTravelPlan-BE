package travel.mytravelplan.domain.comment.exception;

import travel.mytravelplan.global.error.code.TripPlaceReviewCommentErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class TripPlaceReviewCommentException extends BusinessException {
    public TripPlaceReviewCommentException(TripPlaceReviewCommentErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.domain.review.exception;
import travel.mytravelplan.global.error.code.TripPlaceReviewErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class TripPlaceReviewException extends BusinessException {
    public TripPlaceReviewException(TripPlaceReviewErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.domain.trip.exception;

import travel.mytravelplan.global.error.code.TripErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class TripException extends BusinessException {
    public TripException(TripErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.domain.trip.exception;

import travel.mytravelplan.global.error.code.TripJoinErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class TripJoinException extends BusinessException {
    public TripJoinException(TripJoinErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.domain.place.exception;

import travel.mytravelplan.global.error.code.TripPlaceErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class TripPlaceException extends BusinessException {
    public TripPlaceException(TripPlaceErrorCode errorCode) {
        super(errorCode);
    }
}

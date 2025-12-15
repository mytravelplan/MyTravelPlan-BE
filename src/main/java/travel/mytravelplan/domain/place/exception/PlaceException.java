package travel.mytravelplan.domain.place.exception;

import travel.mytravelplan.global.error.code.PlaceErrorCode;
import travel.mytravelplan.global.error.exception.MyTripPlanException;

public class PlaceException extends MyTripPlanException {
    public PlaceException(PlaceErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.domain.place.exception;

import travel.mytravelplan.global.error.code.CustomPlaceErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;


public class CustomPlaceException extends BusinessException {
    public CustomPlaceException(CustomPlaceErrorCode errorCode) {
        super(errorCode);
    }
}

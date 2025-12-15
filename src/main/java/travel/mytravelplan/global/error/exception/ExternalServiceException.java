package travel.mytravelplan.global.error.exception;

import lombok.Getter;
import travel.mytravelplan.global.error.code.ErrorCode;

@Getter
public class ExternalServiceException extends MyTripPlanException {
    protected ExternalServiceException(ErrorCode errorCode) {
        super(errorCode);
    }
}

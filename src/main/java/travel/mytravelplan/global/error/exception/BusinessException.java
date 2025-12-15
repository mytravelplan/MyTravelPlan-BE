package travel.mytravelplan.global.error.exception;

import lombok.Getter;
import travel.mytravelplan.global.error.code.ErrorCode;

@Getter
public class BusinessException extends MyTripPlanException {
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}

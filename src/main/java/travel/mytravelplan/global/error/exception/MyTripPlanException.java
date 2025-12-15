package travel.mytravelplan.global.error.exception;

import lombok.Getter;
import travel.mytravelplan.global.error.code.ErrorCode;

@Getter
public abstract class MyTripPlanException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    protected MyTripPlanException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
}

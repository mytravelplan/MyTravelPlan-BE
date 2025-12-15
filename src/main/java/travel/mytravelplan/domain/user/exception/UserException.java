package travel.mytravelplan.domain.user.exception;

import travel.mytravelplan.global.error.code.UserErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;
import travel.mytravelplan.global.error.code.ErrorCode;

public class UserException extends BusinessException {
    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}

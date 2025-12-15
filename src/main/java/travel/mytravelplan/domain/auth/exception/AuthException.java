package travel.mytravelplan.domain.auth.exception;

import travel.mytravelplan.global.error.code.AuthErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class AuthException extends BusinessException {
    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}

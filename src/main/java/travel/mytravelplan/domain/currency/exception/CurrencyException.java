package travel.mytravelplan.domain.currency.exception;

import travel.mytravelplan.global.error.code.CurrencyErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class CurrencyException extends BusinessException {
    public CurrencyException(CurrencyErrorCode errorCode) {
        super(errorCode);
    }
}

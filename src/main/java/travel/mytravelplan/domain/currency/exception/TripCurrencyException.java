package travel.mytravelplan.domain.currency.exception;

import travel.mytravelplan.global.error.code.CurrencyErrorCode;
import travel.mytravelplan.global.error.code.TripCurrencyErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class TripCurrencyException extends BusinessException {
    public TripCurrencyException(TripCurrencyErrorCode errorCode) {
        super(errorCode);
    }
}

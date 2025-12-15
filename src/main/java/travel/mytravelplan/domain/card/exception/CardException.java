package travel.mytravelplan.domain.card.exception;

import travel.mytravelplan.global.error.code.CardErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class CardException extends BusinessException {
    public CardException(CardErrorCode errorCode) {
        super(errorCode);
    }
}

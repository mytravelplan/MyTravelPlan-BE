package travel.mytravelplan.domain.deck.exception;

import travel.mytravelplan.global.error.code.DeckErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class DeckException extends BusinessException {
    public DeckException(DeckErrorCode errorCode) {
        super(errorCode);
    }
}

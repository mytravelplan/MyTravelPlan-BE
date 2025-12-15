package travel.mytravelplan.domain.checklist.exception;

import travel.mytravelplan.global.error.code.CheckListItemErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class CheckListItemException extends BusinessException {
    public CheckListItemException(CheckListItemErrorCode errorCode) {
        super(errorCode);
    }
}

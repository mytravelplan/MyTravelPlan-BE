package travel.mytravelplan.domain.checklist.exception;

import travel.mytravelplan.global.error.code.CheckListErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class CheckListException extends BusinessException {
    public CheckListException(CheckListErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.domain.diary.exception;

import travel.mytravelplan.global.error.code.DiaryErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class DiaryException extends BusinessException {
    public DiaryException(DiaryErrorCode errorCode) {
        super(errorCode);
    }
}

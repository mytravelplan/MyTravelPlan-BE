package travel.mytravelplan.domain.question.exception;

import travel.mytravelplan.global.error.code.QuestionErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class QuestionException extends BusinessException {
    public QuestionException(QuestionErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.domain.quiz.exception;

import travel.mytravelplan.global.error.code.QuizErrorCode;
import travel.mytravelplan.global.error.code.ScheduleErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class QuizException extends BusinessException {
    public QuizException(QuizErrorCode errorCode) {
        super(errorCode);
    }
}

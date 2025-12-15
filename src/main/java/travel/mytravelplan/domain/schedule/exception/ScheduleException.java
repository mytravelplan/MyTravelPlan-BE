package travel.mytravelplan.domain.schedule.exception;

import travel.mytravelplan.global.error.code.ScheduleErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class ScheduleException extends BusinessException {
    public ScheduleException(ScheduleErrorCode errorCode) {
        super(errorCode);
    }
}

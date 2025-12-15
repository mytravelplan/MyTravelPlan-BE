package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode implements ErrorCode {
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-01", "일정을 찾을 수 없습니다."),
    SCHEDULE_DATE_TIME_OUT_OF_TRIP_RANGE(HttpStatus.BAD_REQUEST, "SCHEDULE-02", "일정 날짜가 여행 날짜 범위를 벗어났습니다."),
    SCHEDULE_NOT_BELONG_TO_TRIP(HttpStatus.BAD_REQUEST, "SCHEDULE-03", "일정이 해당 여행에 속하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

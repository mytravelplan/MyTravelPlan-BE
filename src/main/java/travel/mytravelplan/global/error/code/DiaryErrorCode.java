package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiaryErrorCode implements ErrorCode {
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY-01", "일기를 찾을 수 없습니다."),
    DIARY_NOT_BELONG_TO_TRIP(HttpStatus.BAD_REQUEST, "DIARY-02", "일기가 해당 여행에 속하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

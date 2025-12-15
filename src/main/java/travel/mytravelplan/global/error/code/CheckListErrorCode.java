package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CheckListErrorCode implements ErrorCode {
    CHECK_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "CHECKLIST-01", "체크리스트를 찾을 수 없습니다."),
    CHECK_LIST_NOT_BELONG_TO_TRIP(HttpStatus.BAD_REQUEST, "CHECKLIST-02", "체크리스트가 해당 여행에 속하지 않습니다."),
    CHECK_LIST_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "CHECKLIST-03", "체크리스트 타입이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

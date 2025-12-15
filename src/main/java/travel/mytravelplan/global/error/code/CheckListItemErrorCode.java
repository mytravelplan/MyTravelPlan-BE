package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CheckListItemErrorCode implements ErrorCode {
    CHECK_LIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHECKLIST_ITEM-01", "체크리스트 항목을 찾을 수 없습니다."),
    CHECK_LIST_ITEM_NOT_BELONG_TO_CHECKLIST(HttpStatus.BAD_REQUEST, "CHECKLIST_ITEM-02", "체크리스트 항목이 해당 체크리스트에 속하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

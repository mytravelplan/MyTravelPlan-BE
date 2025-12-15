package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExpenseErrorCode implements ErrorCode {
    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXPENSE-01", "지출를 찾을 수 없습니다."),
    PAYER_NOT_FOUND(HttpStatus.NOT_FOUND, "EXPENSE-02" , "지출의 지불자를 찾을 수 없습니다."),
    INVALID_EXPENSE_TYPE(HttpStatus.BAD_REQUEST, "EXPENSE-03", "지출의 유형이 올바르지 않습니다."),
    INVALID_CALCULATE_TYPE(HttpStatus.BAD_REQUEST, "EXPENSE-03", "지출의 정산 방식이 올바르지 않습니다."),
    EXPENSE_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "EXPENSE-04" , "지출 참여자를 찾을 수 없습니다."),
    DUPLICATE_EXPENSE_PARTICIPANTS(HttpStatus.BAD_REQUEST, "EXPENSE-05", "지출 참여자 목록에 중복된 사용자가 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

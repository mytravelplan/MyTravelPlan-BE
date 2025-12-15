package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BudgetErrorCode implements ErrorCode {
    BUDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "BUDGET-01", "예산을 찾을 수 없습니다."),
    INVALID_BUDGET_TYPE(HttpStatus.BAD_REQUEST, "BUDGET-02", "예산의 유형이 올바르지 않습니다."),
    INVALID_CALCULATE_TYPE(HttpStatus.BAD_REQUEST, "BUDGET-03", "예산의 정산 방식이 올바르지 않습니다."),
    BUDGET_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "BUDGET-04", "예산 참여자를 찾을 수 없습니다."),
    DUPLICATE_BUDGET_PARTICIPANTS(HttpStatus.BAD_REQUEST, "BUDGET-05", "예산 참여자는 중복될 수 없습니다."),
    BUDGET_NOT_BELONG_TO_TRIP(HttpStatus.BAD_REQUEST, "BUDGET-06", "예산이 해당 여행에 속하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

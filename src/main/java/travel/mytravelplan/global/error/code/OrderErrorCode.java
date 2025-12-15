package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-01", "주문 정보를 찾을 수 없습니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "ORDER-02", "배송 중이거나 배송이 완료된 주문은 취소할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

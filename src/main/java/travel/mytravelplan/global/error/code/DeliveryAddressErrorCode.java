package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryAddressErrorCode implements ErrorCode {
    DELIVERY_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY_ADDRESS-01", "배송지를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

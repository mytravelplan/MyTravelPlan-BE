package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripCurrencyErrorCode implements ErrorCode {
    TRIP_CURRENCY_NOT_FOUND(HttpStatus.NOT_FOUND, "TRIP_CURRENCY-01", "여행 통화를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

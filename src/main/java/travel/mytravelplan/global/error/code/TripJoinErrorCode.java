package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripJoinErrorCode implements ErrorCode {
    TRIP_JOIN_NOT_FOUND(HttpStatus.NOT_FOUND, "TRIPJOIN-01", "해당 사용자는 여행에 참여하고 있지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

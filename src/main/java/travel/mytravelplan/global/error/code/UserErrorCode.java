package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-01", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER(HttpStatus.BAD_REQUEST, "USER-02", "이미 존재하는 사용자입니다."),
    USER_CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "USER-03", "자신을 팔로우할 수 없습니다."),
    USER_ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "USER-04", "이미 팔로우하고 있는 사용자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

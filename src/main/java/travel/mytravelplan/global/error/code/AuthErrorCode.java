package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    NOT_FOUND_REFRESH_TOKEN_IN_COOKIE(HttpStatus.BAD_REQUEST, "AUTH-01", "쿠키에서 리프레쉬 토큰을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-02", "유효하지 않은 리프레시 토큰입니다."),
    NOT_FOUND_USER_IN_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-03", "제공된 리프레쉬 토큰으로 사용자를 찾을 수 없습니다."),
    ALREADY_ADDED_ADDITIONAL_INFO(HttpStatus.BAD_REQUEST, "AUTH-04", "추가 정보가 이미 등록되어 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

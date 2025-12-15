package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-01", "서버 내부 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-02", "유효성 검사에 실패했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-03", "지원하지 않는 HTTP 메서드입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-04", "인증되지 않은 사용자입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,"COMMON-05", "접근이 금지된 리소스입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

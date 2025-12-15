package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements ErrorCode {
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY-001", "문의를 찾을 수 없습니다."),
    INQUIRY_NOT_BELONGS_TO_PRODUCT(HttpStatus.BAD_REQUEST, "INQUIRY-002", "문의가 해당 상품에 속하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

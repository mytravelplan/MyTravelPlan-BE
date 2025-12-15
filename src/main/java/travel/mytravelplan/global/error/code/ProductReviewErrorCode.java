package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductReviewErrorCode implements ErrorCode {
    PRODUCT_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_REVIEW-01", "상품 리뷰를 찾을 수 없습니다."),
    PRODUCT_REVIEW_NOT_BELONG_TO_PRODUCT(HttpStatus.BAD_REQUEST, "PRODUCT_REVIEW-02", "상품 리뷰가 해당 상품에 속하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

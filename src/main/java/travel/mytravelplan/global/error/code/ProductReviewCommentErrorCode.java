package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductReviewCommentErrorCode implements ErrorCode {
    PRODUCT_REVIEW_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_REVIEW-01", "상품 리뷰 댓글을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

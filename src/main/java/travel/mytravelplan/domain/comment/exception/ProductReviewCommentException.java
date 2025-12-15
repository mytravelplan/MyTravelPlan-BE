package travel.mytravelplan.domain.comment.exception;

import travel.mytravelplan.global.error.code.ProductReviewCommentErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class ProductReviewCommentException extends BusinessException {
    public ProductReviewCommentException(ProductReviewCommentErrorCode errorCode) {
        super(errorCode);
    }
}

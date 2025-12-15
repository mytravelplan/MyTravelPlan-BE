package travel.mytravelplan.domain.review.exception;
import travel.mytravelplan.global.error.code.ProductReviewErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class ProductReviewException extends BusinessException {
    public ProductReviewException(ProductReviewErrorCode errorCode) {
        super(errorCode);
    }
}

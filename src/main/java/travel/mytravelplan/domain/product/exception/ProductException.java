package travel.mytravelplan.domain.product.exception;

import travel.mytravelplan.global.error.code.ErrorCode;
import travel.mytravelplan.global.error.code.ProductErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class ProductException extends BusinessException {
    public ProductException(ProductErrorCode errorCode) {
        super(errorCode);
    }
}

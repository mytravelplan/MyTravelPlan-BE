package travel.mytravelplan.domain.cart.exception;

import travel.mytravelplan.global.error.code.CartErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class CartException extends BusinessException {
    public CartException(CartErrorCode errorCode) {
        super(errorCode);
    }
}

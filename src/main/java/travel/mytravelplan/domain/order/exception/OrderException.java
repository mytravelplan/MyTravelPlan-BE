package travel.mytravelplan.domain.order.exception;

import travel.mytravelplan.global.error.code.OrderErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class OrderException extends BusinessException {
    public OrderException(OrderErrorCode errorCode) {
        super(errorCode);
    }
}

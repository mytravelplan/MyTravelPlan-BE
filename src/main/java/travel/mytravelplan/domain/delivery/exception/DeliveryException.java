package travel.mytravelplan.domain.delivery.exception;

import travel.mytravelplan.global.error.code.DeliveryErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class DeliveryException extends BusinessException {
    public DeliveryException(DeliveryErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.domain.delivery.exception;

import travel.mytravelplan.global.error.code.DeliveryAddressErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class DeliveryAddressException extends BusinessException {
    public DeliveryAddressException(DeliveryAddressErrorCode errorCode) {
        super(errorCode);
    }
}

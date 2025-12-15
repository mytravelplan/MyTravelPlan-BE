package travel.mytravelplan.domain.inquiry.exception;

import travel.mytravelplan.global.error.code.InquiryErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class InquiryException extends BusinessException {
    public InquiryException(InquiryErrorCode errorCode) {
        super(errorCode);
    }
}

package travel.mytravelplan.infra.s3.exception;

import lombok.Getter;
import travel.mytravelplan.global.error.code.ErrorCode;
import travel.mytravelplan.global.error.exception.ExternalServiceException;

@Getter
public class AwsException extends ExternalServiceException {
    public AwsException(ErrorCode errorCode) {
        super(errorCode);
    }
}

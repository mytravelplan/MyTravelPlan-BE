package travel.mytravelplan.domain.post.exception;

import travel.mytravelplan.global.error.code.PostErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class PostException extends BusinessException {
    public PostException(PostErrorCode errorCode) {
        super(errorCode);
    }
}

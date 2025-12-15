package travel.mytravelplan.domain.comment.exception;

import travel.mytravelplan.global.error.code.PostCommentErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class PostCommentException extends BusinessException {
    public PostCommentException(PostCommentErrorCode errorCode) {
        super(errorCode);
    }
}

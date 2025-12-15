package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostCommentErrorCode implements ErrorCode {
    POST_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_COMMENT-01", "게시글 댓글을 찾을 수 없습니다."),
    POST_COMMENT_NOT_BELONG_TO_POST(HttpStatus.BAD_REQUEST, "POST_COMMENT-02", "게시물 댓글이 해당 게시물에 속하지 않습니다."),;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

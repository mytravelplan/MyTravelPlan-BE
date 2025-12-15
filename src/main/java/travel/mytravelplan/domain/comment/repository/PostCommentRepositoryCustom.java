package travel.mytravelplan.domain.comment.repository;

import travel.mytravelplan.domain.comment.entity.PostComment;

import java.time.LocalDateTime;
import java.util.List;

public interface PostCommentRepositoryCustom {
    List<PostComment> findAllByCursor(Long postCommentId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}

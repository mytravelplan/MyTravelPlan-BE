package travel.mytravelplan.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travel.mytravelplan.domain.comment.entity.PostComment;
import travel.mytravelplan.domain.post.entity.Post;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long>, PostCommentRepositoryCustom {
    @Query("SELECT COUNT(pc) FROM PostComment pc WHERE pc.post = :post")
    long countByPost(Post post);

    @Query("SELECT pc.post.id AS postId, COUNT(pc) AS commentCount " +
            "FROM PostComment pc " +
            "WHERE pc.post.id IN :postIds " +
            "GROUP BY pc.post.id")
    List<PostCommentCountProjection> countCommentsByPostIds(@Param("postIds") List<Long> postIds);

    interface  PostCommentCountProjection {
        Long getPostId();
        Long getCommentCount();
    }
}

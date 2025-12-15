package travel.mytravelplan.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.entity.PostLike;
import travel.mytravelplan.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post")
    long countByPost(Post post);

    Optional<PostLike> findByPostAndUser(Post post, User currentUser);

    boolean existsByPostAndUser(Post post, User user);

    // 여러 게시물의 좋아요 수를 한 번에 조회하는 메서드
    @Query("SELECT pl.post.id AS postId, COUNT(pl) AS likeCount " +
            "FROM PostLike pl " +
            "WHERE pl.post.id IN :postIds " +
            "GROUP BY pl.post.id")
    List<PostLikeCountProjection> countLikesByPostIds(@Param("postIds") List<Long> postIds);

    // 특정 사용자가 좋아요한 게시물 ID 목록 조회
    @Query("SELECT pl.post.id " +
            "FROM PostLike pl " +
            "WHERE pl.user.id = :userId AND pl.post.id IN :postIds")
    List<Long> findLikedPostIdsByUserAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    interface PostLikeCountProjection {
        Long getPostId();
        Long getLikeCount();
    }
}

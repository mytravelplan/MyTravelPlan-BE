package travel.mytravelplan.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.entity.PostBookMark;
import travel.mytravelplan.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PostBookMarkRepository extends JpaRepository<PostBookMark, Long> {
    @Query("SELECT COUNT(pb) FROM PostBookMark pb WHERE pb.post = :post")
    long countByPost(Post post);

    boolean existsByPostAndUser(Post post, User user);

    Optional<PostBookMark> findByPostAndUser(Post post, User currentUser);

    @Query("SELECT pb.post.id " +
            "FROM PostBookMark pb " +
            "WHERE pb.user.id = :userId AND pb.post.id IN :postIds")
    List<Long> findBookmarkedPostIdsByUserAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

}

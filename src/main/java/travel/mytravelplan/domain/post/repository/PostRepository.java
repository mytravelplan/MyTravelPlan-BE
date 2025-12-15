package travel.mytravelplan.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.user.entity.User;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Post> findWithUserById(Long id);
    Long countByUser(User user);
}

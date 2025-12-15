package travel.mytravelplan.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import travel.mytravelplan.domain.user.entity.Follow;
import travel.mytravelplan.domain.user.entity.User;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {
    boolean existsByFollowerAndFollowing(User user, User targetUser);

    void deleteByFollowerAndFollowing(User user, User targetUser);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :id")
    Long countByFollowingId(Long id);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :id")
    Long countByFollowerId(Long id);
}

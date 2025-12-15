package travel.mytravelplan.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.SocialType;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}

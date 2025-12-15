package travel.mytravelplan.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.user.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}

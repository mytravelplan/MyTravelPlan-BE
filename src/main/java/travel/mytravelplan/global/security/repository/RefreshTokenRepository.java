package travel.mytravelplan.global.security.repository;

import org.springframework.data.repository.CrudRepository;
import travel.mytravelplan.domain.user.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}

package travel.mytravelplan.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.post.entity.HashTag;

import java.util.Optional;

public interface HashTagRepository extends JpaRepository<HashTag, Long> {
    Optional<HashTag> findByName(String hashTagName);
}

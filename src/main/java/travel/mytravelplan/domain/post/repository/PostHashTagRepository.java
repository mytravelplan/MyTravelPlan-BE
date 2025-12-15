package travel.mytravelplan.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.post.entity.PostHashTag;

public interface PostHashTagRepository extends JpaRepository<PostHashTag, Long> {
}

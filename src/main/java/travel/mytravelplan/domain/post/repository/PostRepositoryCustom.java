package travel.mytravelplan.domain.post.repository;

import travel.mytravelplan.domain.post.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findAllByCursor(String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}

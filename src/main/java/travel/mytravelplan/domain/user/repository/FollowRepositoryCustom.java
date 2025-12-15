package travel.mytravelplan.domain.user.repository;

import travel.mytravelplan.domain.user.entity.Follow;

import java.util.List;

public interface FollowRepositoryCustom {
    List<Follow> findAllFollowings(Long userId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
    List<Follow> findAllFollowers(Long userId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}

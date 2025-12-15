package travel.mytravelplan.domain.user.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.user.entity.Follow;
import travel.mytravelplan.domain.user.entity.QFollow;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QFollow follow = QFollow.follow;

    @Override
    public List<Follow> findAllFollowers(Long userId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(userId != null) {
            booleanBuilder.and(follow.following.id.eq(userId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(follow.follower.username.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(follow.createdAt.gt(createdAt)
                            .or(follow.createdAt.eq(createdAt).and(follow.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(follow.createdAt.lt(createdAt)
                            .or(follow.createdAt.eq(createdAt).and(follow.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? follow.createdAt.asc() : follow.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? follow.id.asc() : follow.id.desc();

        return queryFactory.selectFrom(follow)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Follow> findAllFollowings(Long userId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(userId != null) {
            booleanBuilder.and(follow.follower.id.eq(userId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(follow.following.username.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(follow.createdAt.gt(createdAt)
                            .or(follow.createdAt.eq(createdAt).and(follow.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(follow.createdAt.lt(createdAt)
                            .or(follow.createdAt.eq(createdAt).and(follow.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? follow.createdAt.asc() : follow.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? follow.id.asc() : follow.id.desc();

        return queryFactory.selectFrom(follow)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

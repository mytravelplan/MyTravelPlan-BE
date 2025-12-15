package travel.mytravelplan.domain.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.entity.QPost;
import travel.mytravelplan.domain.user.entity.QUser;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;
    private final QUser user = QUser.user;

    @Override
    public List<Post> findAllByCursor(String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(username != null) {
            booleanBuilder.and(post.user.username.eq(username));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(post.content.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(post.createdAt.gt(createdAt)
                            .or(post.createdAt.eq(createdAt).and(post.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(post.createdAt.lt(createdAt)
                            .or(post.createdAt.eq(createdAt).and(post.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? post.createdAt.asc() : post.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? post.id.asc() : post.id.desc();

        return queryFactory
                .select(post)
                .from(post)
                .join(post.user, user).fetchJoin()
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

package travel.mytravelplan.domain.comment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.comment.entity.PostComment;
import travel.mytravelplan.domain.comment.entity.QPostComment;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PostCommentRepositoryImpl implements PostCommentRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QPostComment postComment = QPostComment.postComment;

    @Override
    public List<PostComment> findAllByCursor(Long postCommentId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(postCommentId != null) {
            booleanBuilder.and(postComment.post.id.eq(postCommentId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(postComment.content.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(postComment.createdAt.gt(createdAt)
                            .or(postComment.createdAt.eq(createdAt).and(postComment.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(postComment.createdAt.lt(createdAt)
                            .or(postComment.createdAt.eq(createdAt).and(postComment.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? postComment.createdAt.asc() : postComment.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? postComment.id.asc() : postComment.id.desc();

        return queryFactory
                .selectFrom(postComment)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

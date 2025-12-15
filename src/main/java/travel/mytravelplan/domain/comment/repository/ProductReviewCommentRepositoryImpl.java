package travel.mytravelplan.domain.comment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.comment.entity.ProductReviewComment;
import travel.mytravelplan.domain.comment.entity.QProductReviewComment;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ProductReviewCommentRepositoryImpl implements ProductReviewCommentCustom {
    private final JPAQueryFactory queryFactory;
    private final QProductReviewComment productReviewComment = QProductReviewComment.productReviewComment;

    @Override
    public List<ProductReviewComment> findAllByCursor(Long productReviewId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(productReviewId != null) {
            booleanBuilder.and(productReviewComment.productReview.id.eq(productReviewId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(productReviewComment.content.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(productReviewComment.createdAt.gt(createdAt)
                            .or(productReviewComment.createdAt.eq(createdAt).and(productReviewComment.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(productReviewComment.createdAt.lt(createdAt)
                            .or(productReviewComment.createdAt.eq(createdAt).and(productReviewComment.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? productReviewComment.createdAt.asc() : productReviewComment.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? productReviewComment.id.asc() : productReviewComment.id.desc();

        return queryFactory.selectFrom(productReviewComment)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

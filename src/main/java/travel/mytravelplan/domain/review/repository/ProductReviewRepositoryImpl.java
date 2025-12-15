package travel.mytravelplan.domain.review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.review.entity.QProductReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ProductReviewRepositoryImpl implements ProductReviewRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QProductReview productReview = QProductReview.productReview;

    @Override
    public List<ProductReview> findAllByCursor(Long productId, String keyword, Boolean imgOnly, BigDecimal rating, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(productId != null) {
            booleanBuilder.and(productReview.product.id.eq(productId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(productReview.content.containsIgnoreCase(keyword));
        }

        if (imgOnly != null && imgOnly) {
            booleanBuilder.and(productReview.imageUrls.isNotEmpty());
        }

        if (rating != null) {
            booleanBuilder.and(productReview.rating.eq(rating));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(productReview.createdAt.gt(createdAt)
                            .or(productReview.createdAt.eq(createdAt).and(productReview.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(productReview.createdAt.lt(createdAt)
                            .or(productReview.createdAt.eq(createdAt).and(productReview.id.lt(after))));
                }
            } else if (orderBy.equals("rating")) {
                BigDecimal afterRating = new BigDecimal(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(productReview.rating.gt(afterRating))
                            .or(productReview.rating.eq(afterRating).and(productReview.id.gt(after)));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(productReview.rating.lt(afterRating))
                            .or(productReview.rating.eq(afterRating).and(productReview.id.lt(after)));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? productReview.createdAt.asc() : productReview.createdAt.desc();
        } else if (orderBy.equals("rating")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? productReview.rating.asc() : productReview.rating.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? productReview.id.asc() : productReview.id.desc();

        return queryFactory
                .selectFrom(productReview)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

package travel.mytravelplan.domain.review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.review.entity.QTripPlaceReview;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TripPlaceReviewRepositoryImpl implements TripPlaceReviewRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QTripPlaceReview tripPlaceReview = QTripPlaceReview.tripPlaceReview;

    @Override
    public List<TripPlaceReview> findAllByCursor(Long tripPlaceId, String keyword, Boolean imgOnly, BigDecimal rating, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(tripPlaceId != null) {
            booleanBuilder.and(tripPlaceReview.tripPlace.id.eq(tripPlaceId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(tripPlaceReview.content.containsIgnoreCase(keyword));
        }

        if (imgOnly != null && imgOnly) {
            booleanBuilder.and(tripPlaceReview.imageUrls.isNotEmpty());
        }

        if (rating != null) {
            booleanBuilder.and(tripPlaceReview.rating.eq(rating));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(tripPlaceReview.createdAt.gt(createdAt)
                            .or(tripPlaceReview.createdAt.eq(createdAt).and(tripPlaceReview.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(tripPlaceReview.createdAt.lt(createdAt)
                            .or(tripPlaceReview.createdAt.eq(createdAt).and(tripPlaceReview.id.lt(after))));
                }
            } else if (orderBy.equals("rating")) {
                BigDecimal afterRating = new BigDecimal(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(tripPlaceReview.rating.gt(afterRating)
                            .or(tripPlaceReview.rating.eq(afterRating).and(tripPlaceReview.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(tripPlaceReview.rating.lt(afterRating)
                            .or(tripPlaceReview.rating.eq(afterRating).and(tripPlaceReview.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? tripPlaceReview.createdAt.asc() : tripPlaceReview.createdAt.desc();
        } else if (orderBy.equals("rating")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? tripPlaceReview.rating.asc() : tripPlaceReview.rating.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? tripPlaceReview.id.asc() : tripPlaceReview.id.desc();

        return queryFactory
                .selectFrom(tripPlaceReview)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

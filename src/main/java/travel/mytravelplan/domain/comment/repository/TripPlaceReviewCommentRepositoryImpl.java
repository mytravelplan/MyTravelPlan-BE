package travel.mytravelplan.domain.comment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.comment.entity.QTripPlaceReviewComment;
import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TripPlaceReviewCommentRepositoryImpl implements TripPlaceReviewCommentCustom {
    private final JPAQueryFactory queryFactory;
    private final QTripPlaceReviewComment tripPlaceReviewComment = QTripPlaceReviewComment.tripPlaceReviewComment;

    @Override
    public List<TripPlaceReviewComment> findAllByCursor(Long tripPlaceReviewId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        
        if(tripPlaceReviewId != null){
            booleanBuilder.and(tripPlaceReviewComment.tripPlaceReview.id.eq(tripPlaceReviewId));
        }
        
        if(StringUtils.hasText(keyword)){
            booleanBuilder.and(tripPlaceReviewComment.content.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(tripPlaceReviewComment.createdAt.gt(createdAt)
                            .or(tripPlaceReviewComment.createdAt.eq(createdAt).and(tripPlaceReviewComment.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(tripPlaceReviewComment.createdAt.lt(createdAt)
                            .or(tripPlaceReviewComment.createdAt.eq(createdAt).and(tripPlaceReviewComment.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? tripPlaceReviewComment.createdAt.asc() : tripPlaceReviewComment.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? tripPlaceReviewComment.id.asc() : tripPlaceReviewComment.id.desc();

        return queryFactory
                .selectFrom(tripPlaceReviewComment)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

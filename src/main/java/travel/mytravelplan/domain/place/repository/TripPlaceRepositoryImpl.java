package travel.mytravelplan.domain.place.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import travel.mytravelplan.domain.place.entity.QTripPlace;
import travel.mytravelplan.domain.place.entity.TripPlace;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TripPlaceRepositoryImpl implements TripPlaceRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QTripPlace tripPlace = QTripPlace.tripPlace;

    @Override
    public List<TripPlace> findAllByCursor(String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(tripPlace.createdAt.gt(createdAt)
                            .or(tripPlace.createdAt.eq(createdAt).and(tripPlace.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(tripPlace.createdAt.lt(createdAt)
                            .or(tripPlace.createdAt.eq(createdAt).and(tripPlace.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? tripPlace.createdAt.asc() : tripPlace.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? tripPlace.id.asc() : tripPlace.id.desc();

        return queryFactory.selectFrom(tripPlace)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

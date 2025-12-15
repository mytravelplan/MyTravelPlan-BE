package travel.mytravelplan.domain.trip.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.trip.entity.QTrip;
import travel.mytravelplan.domain.trip.entity.QTripJoin;
import travel.mytravelplan.domain.trip.entity.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TripRepositoryImpl implements TripRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Trip> findAllByUserCursor(String username, String orderBy, String direction, String cursor, Long after, int limit) {
        QTrip trip = QTrip.trip;
        QTripJoin tripJoin = QTripJoin.tripJoin;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(StringUtils.hasText(username)) {
            booleanBuilder.and(tripJoin.user.username.eq(username));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(trip.createdAt.gt(createdAt)
                            .or(trip.createdAt.eq(createdAt).and(trip.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(trip.createdAt.lt(createdAt)
                            .or(trip.createdAt.eq(createdAt).and(trip.id.lt(after))));
                }
            } else if(orderBy.equals("startDate")) {
                LocalDate startDate = LocalDate.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(trip.startDate.gt(startDate)
                            .or(trip.startDate.eq(startDate).and(trip.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(trip.startDate.lt(startDate)
                            .or(trip.startDate.eq(startDate).and(trip.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? trip.createdAt.asc() : trip.createdAt.desc();
        } else if (orderBy.equals("startDate")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? trip.startDate.asc() : trip.startDate.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? trip.id.asc() : trip.id.desc();

        return queryFactory.selectFrom(trip)
                .join(tripJoin).on(trip.id.eq(tripJoin.trip.id))
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

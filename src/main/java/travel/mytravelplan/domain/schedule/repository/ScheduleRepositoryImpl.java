package travel.mytravelplan.domain.schedule.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.schedule.entity.QSchedule;
import travel.mytravelplan.domain.schedule.entity.Schedule;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QSchedule schedule = QSchedule.schedule;

    @Override
    public List<Schedule> findAllByCursor(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (tripId != null) {
            booleanBuilder.and(schedule.trip.id.eq(tripId));
        }

        if (StringUtils.hasText(keyword)) {
            booleanBuilder.and(schedule.title.containsIgnoreCase(keyword));
        }

        if (cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(schedule.createdAt.gt(createdAt)
                            .or(schedule.createdAt.eq(createdAt).and(schedule.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(schedule.createdAt.lt(createdAt)
                            .or(schedule.createdAt.eq(createdAt).and(schedule.id.lt(after))));
                }
            } else if(orderBy.equals("displayOrder")) {
                Long displayOrder = Long.parseLong(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(schedule.displayOrder.gt(displayOrder)
                            .or(schedule.displayOrder.eq(displayOrder).and(schedule.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(schedule.displayOrder.lt(displayOrder)
                            .or(schedule.displayOrder.eq(displayOrder).and(schedule.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? schedule.createdAt.asc() : schedule.createdAt.desc();
        } else if (orderBy.equals("displayOrder")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? schedule.displayOrder.asc() : schedule.displayOrder.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? schedule.id.asc() : schedule.id.desc();

        return queryFactory
                .selectFrom(schedule)
                .leftJoin(schedule.place).fetchJoin()
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }

    @Override
    public Long findMaxDisplayOrderByTripId(Long tripId) {
        Long max = queryFactory
                .select(schedule.displayOrder.max())
                .from(schedule)
                .where(schedule.trip.id.eq(tripId))
                .fetchOne();

        return max != null ? max : 0L;
    }
}

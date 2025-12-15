package travel.mytravelplan.domain.checklist.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.checklist.entity.CheckList;
import travel.mytravelplan.domain.checklist.entity.QCheckList;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CheckListRepositoryImpl implements CheckListRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QCheckList checkList = QCheckList.checkList;

    @Override
    public List<CheckList> findAllByCursor(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(tripId != null) {
            booleanBuilder.and(checkList.trip.id.eq(tripId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(checkList.name.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if(orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(checkList.createdAt.gt(createdAt)
                            .or(checkList.createdAt.eq(createdAt).and(checkList.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(checkList.createdAt.lt(createdAt)
                            .or(checkList.createdAt.eq(createdAt).and(checkList.id.lt(after))));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? checkList.createdAt.asc() : checkList.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? checkList.id.asc() : checkList.id.desc();

        return queryFactory
                .selectFrom(checkList)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }
}

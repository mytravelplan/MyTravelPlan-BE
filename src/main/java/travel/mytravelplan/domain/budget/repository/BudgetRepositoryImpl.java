package travel.mytravelplan.domain.budget.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.budget.entity.Budget;
import travel.mytravelplan.domain.budget.entity.QBudget;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class BudgetRepositoryImpl implements BudgetRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QBudget budget = QBudget.budget;

    @Override
    public List<Budget> findAllByCursor(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(tripId != null) {
            booleanBuilder.and(budget.trip.id.eq(tripId));
        }

        if(StringUtils.hasText(keyword)) {
            booleanBuilder.and(budget.memo.containsIgnoreCase(keyword));
        }

        if(cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(budget.createdAt.gt(createdAt)
                            .or(budget.createdAt.eq(createdAt).and(budget.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(budget.createdAt.lt(createdAt))
                            .or(budget.createdAt.eq(createdAt).and(budget.id.lt(after)));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = direction.equalsIgnoreCase("asc") ? budget.createdAt.asc() : budget.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = direction.equalsIgnoreCase("asc") ? budget.id.asc() : budget.id.desc();

        return queryFactory.selectFrom(budget)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }

}

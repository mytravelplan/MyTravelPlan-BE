package travel.mytravelplan.domain.expense.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import travel.mytravelplan.domain.expense.dto.ExpenseRatioDto;
import travel.mytravelplan.domain.expense.dto.ExpenseStatisticsDto;
import travel.mytravelplan.domain.expense.entity.*;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.GroupByType;
import travel.mytravelplan.domain.schedule.entity.QSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ExpenseRepositoryImpl implements ExpenseRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QExpense expense = QExpense.expense;
    private final QSchedule schedule = QSchedule.schedule;

    @Override
    public List<Expense> findAllByCursor(Long scheduleId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (scheduleId != null) {
            booleanBuilder.and(expense.schedule.id.eq(scheduleId));
        }

        if (StringUtils.hasText(keyword)) {
            booleanBuilder.and(expense.memo.containsIgnoreCase(keyword));
        }

        if (cursor != null && after != null) {
            if (orderBy.equals("createdAt")) {
                LocalDateTime createdAt = LocalDateTime.parse(cursor);
                if (direction.equalsIgnoreCase("asc")) {
                    booleanBuilder.and(expense.createdAt.gt(createdAt)
                            .or(expense.createdAt.eq(createdAt).and(expense.id.gt(after))));
                } else if (direction.equalsIgnoreCase("desc")) {
                    booleanBuilder.and(expense.createdAt.lt(createdAt))
                            .or(expense.createdAt.eq(createdAt).and(expense.id.lt(after)));
                }
            }
        }

        OrderSpecifier<?> firstOrder = null;

        if (orderBy.equals("createdAt")) {
            firstOrder = "asc".equalsIgnoreCase(direction) ? expense.createdAt.asc() : expense.createdAt.desc();
        }

        OrderSpecifier<?> secondOrder = "asc".equalsIgnoreCase(direction) ? expense.id.asc() : expense.id.desc();

        return queryFactory.selectFrom(expense)
                .where(booleanBuilder)
                .orderBy(firstOrder, secondOrder)
                .limit(limit)
                .fetch();
    }

    @Override
    public ExpenseStatisticsDto getExpenseStatistics(Long tripId, ExpenseType expenseType, GroupByType groupBy, LocalDate date) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (tripId != null) {
            booleanBuilder.and(schedule.trip.id.eq(tripId));
        }

        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            booleanBuilder.and(expense.dateTime.between(start, end));
        }

        BigDecimal totalAmount = null;
        List<ExpenseRatioDto> statistics = new ArrayList<>();

        if (expenseType != null) {
            if (expenseType.equals(ExpenseType.PERSONAL)) {
                QPersonalExpense personalExpense = expense.as(QPersonalExpense.class);

                // 1. 총합 조회
                totalAmount = queryFactory
                        .select(personalExpense.totalAmount.sum())
                        .from(expense)
                        .join(expense.schedule, schedule)
                        .where(booleanBuilder)
                        .fetchOne();

                // 2. 통계 조회
                statistics = queryFactory
                        .select(Projections.constructor(
                                ExpenseRatioDto.class,
                                expense.category,
                                personalExpense.totalAmount.sum(),
                                personalExpense.totalAmount.sum()
                                        .multiply(100).divide(totalAmount == null ? BigDecimal.ONE : totalAmount)
                        ))
                        .from(expense)
                        .join(expense.schedule, schedule)
                        .where(booleanBuilder)
                        .groupBy(expense.category)
                        .fetch();

            } else {
                QSharedExpense sharedExpense = expense.as(QSharedExpense.class);
                QExpenseParticipant expenseParticipant = QExpenseParticipant.expenseParticipant;

                // 1. 총합 조회
                totalAmount = queryFactory
                        .select(expenseParticipant.amount.sum())
                        .from(expense)
                        .join(expense.schedule, schedule)
                        .join(sharedExpense.expenseParticipants, expenseParticipant)
                        .where(booleanBuilder)
                        .fetchOne();

                // 2. 통계 조회
                statistics = queryFactory
                        .select(Projections.constructor(
                                ExpenseRatioDto.class,
                                expense.category,
                                expenseParticipant.amount.sum(),
                                expenseParticipant.amount.sum()
                                        .multiply(100).divide(totalAmount == null ? BigDecimal.ONE : totalAmount)
                        ))
                        .from(expense)
                        .join(expense.schedule, schedule)
                        .join(sharedExpense.expenseParticipants, expenseParticipant)
                        .where(booleanBuilder)
                        .groupBy(expense.category)
                        .fetch();
            }
        }

        return ExpenseStatisticsDto.builder()
                .totalAmount(totalAmount == null ? BigDecimal.ZERO : totalAmount)
                .statistics(statistics)
                .groupBy(groupBy)
                .build();
    }
}
package travel.mytravelplan.domain.expense.repository;

import travel.mytravelplan.domain.expense.dto.ExpenseStatisticsDto;
import travel.mytravelplan.domain.expense.entity.Expense;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.GroupByType;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepositoryCustom {
    List<Expense> findAllByCursor(Long scheduleId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
    ExpenseStatisticsDto getExpenseStatistics(Long tripId, ExpenseType expenseType, GroupByType groupBy, LocalDate date);
}

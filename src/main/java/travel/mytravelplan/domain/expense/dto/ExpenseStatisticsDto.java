package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.expense.enums.GroupByType;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ExpenseStatisticsDto {
    BigDecimal totalAmount;
    List<ExpenseRatioDto> statistics;
    GroupByType groupBy;

    @Builder
    private ExpenseStatisticsDto(BigDecimal totalAmount, List<ExpenseRatioDto> statistics, GroupByType groupBy) {
        this.totalAmount = totalAmount;
        this.statistics = statistics;
        this.groupBy = groupBy;
    }
}

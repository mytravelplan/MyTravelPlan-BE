package travel.mytravelplan.domain.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ExpenseRatioDto {
    private ExpenseCategory expenseCategory;
    private BigDecimal amount;
    private BigDecimal percentage;
}

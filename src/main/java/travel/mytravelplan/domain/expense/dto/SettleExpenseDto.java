package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.budget.dto.BudgetSettleDto;

import java.util.List;

@Getter
public class SettleExpenseDto {
    BudgetSettleDto budget;
    List<UserExpenseDto> expenseList;
    List<TransferDto> transferList;

    @Builder
    private SettleExpenseDto(BudgetSettleDto budget, List<UserExpenseDto> expenseList, List<TransferDto> transferList) {
        this.budget = budget;
        this.expenseList = expenseList;
        this.transferList = transferList;
    }
}
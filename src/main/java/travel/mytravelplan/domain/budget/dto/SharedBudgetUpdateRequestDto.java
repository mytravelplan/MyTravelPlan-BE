package travel.mytravelplan.domain.budget.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.budget.enums.BudgetType;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class SharedBudgetUpdateRequestDto extends BudgetUpdateRequestDto {
    private CalculateType calculateType;
    private List<BudgetParticipantRequestDto> budgetParticipants;

    @Builder
    private SharedBudgetUpdateRequestDto(BudgetType budgetType, LocalDateTime datetime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, CalculateType calculateType, List<BudgetParticipantRequestDto> budgetParticipants) {
        super(budgetType, datetime, memo, paymentMethod, currencyType, exchangeRate);
        this.calculateType = calculateType;
        this.budgetParticipants = budgetParticipants;
    }
}

package travel.mytravelplan.domain.budget.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.budget.enums.BudgetType;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PersonalBudgetUpdateRequestDto extends BudgetUpdateRequestDto {
    private BigDecimal totalAmount;

    @Builder
    private PersonalBudgetUpdateRequestDto(BudgetType budgetType, LocalDateTime datetime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount) {
        super(budgetType, datetime, memo, paymentMethod, currencyType, exchangeRate);
        this.totalAmount = totalAmount;
    }
}

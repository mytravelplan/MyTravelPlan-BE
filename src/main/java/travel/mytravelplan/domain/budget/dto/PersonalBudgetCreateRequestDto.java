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
public class PersonalBudgetCreateRequestDto extends BudgetCreateRequestDto {
    private BigDecimal totalAmount;

    @Builder
    private PersonalBudgetCreateRequestDto(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount) {
        super(BudgetType.PERSONAL, dateTime, memo, paymentMethod, currencyType, exchangeRate);
        this.totalAmount = totalAmount;
    }
}

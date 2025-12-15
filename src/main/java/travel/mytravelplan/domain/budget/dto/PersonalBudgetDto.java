package travel.mytravelplan.domain.budget.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PersonalBudgetDto extends BudgetDto {
    private BigDecimal totalAmount;

    @Builder
    private PersonalBudgetDto(Long id, LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, BigDecimal totalAmount) {
        super(id, dateTime, memo, paymentMethod, currencyType, exchangeRate);
        this.totalAmount = totalAmount;
    }
}

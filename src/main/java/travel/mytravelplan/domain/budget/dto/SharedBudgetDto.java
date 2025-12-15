package travel.mytravelplan.domain.budget.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SharedBudgetDto extends BudgetDto {
    private CalculateType calculateType;
    private List<BudgetParticipantDto> budgetParticipants;

    @Builder
    private SharedBudgetDto(Long id, LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, CalculateType calculateType, List<BudgetParticipantDto> budgetParticipants) {
        super(id, dateTime, memo, paymentMethod, currencyType, exchangeRate);
        this.calculateType = calculateType;
        this.budgetParticipants = budgetParticipants;
    }
}

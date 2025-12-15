package travel.mytravelplan.domain.budget.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public abstract class BudgetDto {
    private Long id;

    private LocalDateTime dateTime;

    private String memo;

    private PaymentMethod paymentMethod;

    private CurrencyType currencyType;

    private BigDecimal exchangeRate;

    protected BudgetDto(Long id, LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate) {
        this.id = id;
        this.dateTime = dateTime;
        this.memo = memo;
        this.paymentMethod = paymentMethod;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
    }
}

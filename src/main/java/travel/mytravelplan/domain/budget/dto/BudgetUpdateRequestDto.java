package travel.mytravelplan.domain.budget.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.budget.enums.BudgetType;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "budgetType",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SharedBudgetUpdateRequestDto.class, name = "SHARED"),
        @JsonSubTypes.Type(value = PersonalBudgetUpdateRequestDto.class, name = "PERSONAL"),
})
public abstract class BudgetUpdateRequestDto {
    private BudgetType budgetType;
    private LocalDateTime dateTime;
    private String memo;
    private PaymentMethod paymentMethod;
    private CurrencyType currencyType;
    private BigDecimal exchangeRate;

    protected BudgetUpdateRequestDto(BudgetType budgetType, LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate) {
        this.budgetType = budgetType;
        this.dateTime = dateTime;
        this.memo = memo;
        this.paymentMethod = paymentMethod;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
    }
}

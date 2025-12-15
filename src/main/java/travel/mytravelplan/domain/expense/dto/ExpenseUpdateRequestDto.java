package travel.mytravelplan.domain.expense.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "expenseType",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SharedExpenseUpdateRequestDto.class, name = "SHARED"),
        @JsonSubTypes.Type(value = PersonalExpenseUpdateRequestDto.class, name = "PERSONAL"),
})
public abstract class ExpenseUpdateRequestDto {
    private ExpenseType expenseType;
    private LocalDateTime dateTime;
    private String memo;
    private PaymentMethod paymentMethod;
    private CurrencyType currencyType;
    private BigDecimal exchangeRate;
    private ExpenseCategory category;
    private List<String> imageUrls;

    protected ExpenseUpdateRequestDto(ExpenseType expenseType, LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, ExpenseCategory category, List<String> imageUrls) {
        this.expenseType = expenseType;
        this.dateTime = dateTime;
        this.memo = memo;
        this.paymentMethod = paymentMethod;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
        this.category = category;
        this.imageUrls = imageUrls;
    }
}

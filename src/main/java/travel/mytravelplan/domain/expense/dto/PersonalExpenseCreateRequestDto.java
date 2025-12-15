package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class PersonalExpenseCreateRequestDto extends ExpenseCreateRequestDto {
    private BigDecimal totalAmount;

    @Builder
    private PersonalExpenseCreateRequestDto(ExpenseType expenseType, LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, ExpenseCategory category, List<String> imageUrls, BigDecimal totalAmount) {
        super(expenseType, dateTime, memo, paymentMethod, currencyType, exchangeRate, category, imageUrls);
        this.totalAmount = totalAmount;
    }
}

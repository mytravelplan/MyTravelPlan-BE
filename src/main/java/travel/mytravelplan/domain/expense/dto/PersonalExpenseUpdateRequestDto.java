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
public class PersonalExpenseUpdateRequestDto extends ExpenseUpdateRequestDto {
    private BigDecimal totalAmount;

    @Builder
    private PersonalExpenseUpdateRequestDto(LocalDateTime datetime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, ExpenseCategory category, List<String> imageUrls, BigDecimal totalAmount) {
        super(ExpenseType.PERSONAL, datetime, memo, paymentMethod, currencyType, exchangeRate, category, imageUrls);
        this.totalAmount = totalAmount;
    }
}

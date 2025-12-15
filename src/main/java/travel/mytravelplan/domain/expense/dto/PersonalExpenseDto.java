package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PersonalExpenseDto extends ExpenseDto {
    private BigDecimal totalAmount;

    @Builder
    private PersonalExpenseDto(Long id, LocalDateTime datetime, String memo, BigDecimal totalAmount, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, ExpenseCategory category, List<String> imageUrls) {
        super(id, datetime, memo, paymentMethod, currencyType, exchangeRate, category, imageUrls);
        this.totalAmount = totalAmount;
    }
}

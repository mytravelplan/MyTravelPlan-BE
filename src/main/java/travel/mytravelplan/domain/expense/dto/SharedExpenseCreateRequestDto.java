package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class SharedExpenseCreateRequestDto extends ExpenseCreateRequestDto {
    private CalculateType calculateType;
    private Long payerId;
    private List<ExpenseParticipantRequestDto> expenseParticipants;

    @Builder
    private SharedExpenseCreateRequestDto(ExpenseType expenseType, LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, ExpenseCategory category, List<String> imageUrls, CalculateType calculateType, Long payerId, List<ExpenseParticipantRequestDto> expenseParticipants) {
        super(expenseType, dateTime, memo, paymentMethod, currencyType, exchangeRate, category, imageUrls);
        this.calculateType = calculateType;
        this.payerId = payerId;
        this.expenseParticipants = expenseParticipants;
    }
}

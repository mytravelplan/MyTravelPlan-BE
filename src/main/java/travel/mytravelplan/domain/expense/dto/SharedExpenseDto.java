package travel.mytravelplan.domain.expense.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SharedExpenseDto extends ExpenseDto {
    private CalculateType calculateType;
    private Long payerId;
    private List<ExpenseParticipantDto> expenseParticipants;

    @Builder
    private SharedExpenseDto(Long id, LocalDateTime datetime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, ExpenseCategory category, List<String> imageUrls, CalculateType calculateType, Long payerId, List<ExpenseParticipantDto> expenseParticipants) {
        super(id, datetime, memo, paymentMethod, currencyType, exchangeRate, category, imageUrls);
        this.calculateType = calculateType;
        this.payerId = payerId;
        this.expenseParticipants = expenseParticipants;
    }
}

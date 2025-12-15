package travel.mytravelplan.domain.expense.dto;

import lombok.Getter;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public abstract class ExpenseDto {
    private Long id;
    private LocalDateTime dateTime;
    private String memo;
    private PaymentMethod paymentMethod;
    private CurrencyType currencyType;
    private BigDecimal exchangeRate;
    private ExpenseCategory category;
    private List<String> imageUrls;

    protected ExpenseDto(Long id, LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, CurrencyType currencyType, BigDecimal exchangeRate, ExpenseCategory category, List<String> imageUrls) {
        this.id = id;
        this.dateTime = dateTime;
        this.memo = memo;
        this.paymentMethod = paymentMethod;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
        this.category = category;
        this.imageUrls = imageUrls;
    }
}

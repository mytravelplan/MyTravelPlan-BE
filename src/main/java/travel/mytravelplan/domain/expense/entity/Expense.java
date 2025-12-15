package travel.mytravelplan.domain.expense.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Expense extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateTime;

    private String memo;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    private BigDecimal exchangeRate;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    protected Expense(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, ExpenseCategory category, CurrencyType currencyType, BigDecimal exchangeRate, Schedule schedule) {
        this.dateTime = dateTime;
        this.memo = memo;
        this.paymentMethod = paymentMethod;
        this.category = category;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
        this.schedule = schedule;
    }

    protected void update(LocalDateTime dateTime, String memo, PaymentMethod paymentMethod, ExpenseCategory category, CurrencyType currencyType, BigDecimal exchangeRate) {
        this.dateTime = dateTime;
        this.memo = memo;
        this.paymentMethod = paymentMethod;
        this.category = category;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
    }
}

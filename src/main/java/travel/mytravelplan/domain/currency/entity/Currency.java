package travel.mytravelplan.domain.currency.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Currency extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    private BigDecimal exchangeRate;

    @Builder(access = AccessLevel.PRIVATE)
    private Currency(CurrencyType currencyType, BigDecimal exchangeRate) {
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
    }

    public static Currency createCurrency(CurrencyType currencyType, BigDecimal exchangeRate) {
        return Currency.builder()
                .currencyType(currencyType)
                .exchangeRate(exchangeRate)
                .build();
    }
}

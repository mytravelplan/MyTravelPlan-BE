package travel.mytravelplan.domain.currency.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.currency.enums.CurrencyType;

import java.math.BigDecimal;

@Getter
public class TripCurrencyDto {
    private String name;
    private CurrencyType currencyType;
    private BigDecimal exchangeRate;

    @Builder
    private TripCurrencyDto(String name, CurrencyType currencyType, BigDecimal exchangeRate) {
        this.name = name;
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
    }
}

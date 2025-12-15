package travel.mytravelplan.domain.currency.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import travel.mytravelplan.domain.currency.dto.CurrencyDto;
import travel.mytravelplan.domain.currency.entity.Currency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.exception.CurrencyException;
import travel.mytravelplan.domain.currency.mapper.CurrencyMapper;
import travel.mytravelplan.domain.currency.repository.CurrencyRepository;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("환율 서비스 테스트")
class CurrencyServiceTest extends ServiceTestSupport {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyMapper currencyMapper;

    @InjectMocks
    private CurrencyService currencyService;

    private Currency currency;
    private CurrencyDto currencyDto;

    @BeforeEach
    void setUp() {
        currency = Currency.createCurrency(CurrencyType.USD, new BigDecimal("1300.00"));

        currencyDto = CurrencyDto.builder()
                .name("미국 달러")
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300.00"))
                .build();
    }

    @Test
    @DisplayName("환율 조회 성공")
    void getCurrency_Success() {
        // given
        given(currencyRepository.findByCurrencyType(eq(CurrencyType.USD))).willReturn(Optional.of(currency));
        given(currencyMapper.toDto(any(Currency.class))).willReturn(currencyDto);

        // when
        CurrencyDto result = currencyService.getCurrency(CurrencyType.USD);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCurrencyType()).isEqualTo(CurrencyType.USD);
        assertThat(result.getExchangeRate()).isEqualTo(new BigDecimal("1300.00"));
        assertThat(result.getName()).isEqualTo("미국 달러");

        then(currencyRepository).should().findByCurrencyType(eq(CurrencyType.USD));
        then(currencyMapper).should().toDto(any(Currency.class));
    }

    @Test
    @DisplayName("환율 조회 실패 - 존재하지 않는 환율")
    void getCurrency_NotFound() {
        // given
        given(currencyRepository.findByCurrencyType(eq(CurrencyType.USD))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> currencyService.getCurrency(CurrencyType.USD))
                .isInstanceOf(CurrencyException.class);

        then(currencyRepository).should().findByCurrencyType(eq(CurrencyType.USD));
    }

    @Test
    @DisplayName("전체 환율 목록 조회 성공")
    void getAllCurrencies_Success() {
        // given
        Currency currency2 = Currency.createCurrency(CurrencyType.EUR, new BigDecimal("1400.00"));
        Currency currency3 = Currency.createCurrency(CurrencyType.JPY, new BigDecimal("900.00"));

        CurrencyDto currencyDto2 = CurrencyDto.builder()
                .name("유로")
                .currencyType(CurrencyType.EUR)
                .exchangeRate(new BigDecimal("1400.00"))
                .build();

        CurrencyDto currencyDto3 = CurrencyDto.builder()
                .name("일본 엔")
                .currencyType(CurrencyType.JPY)
                .exchangeRate(new BigDecimal("900.00"))
                .build();

        List<Currency> currencies = Arrays.asList(currency, currency2, currency3);

        given(currencyRepository.findAll()).willReturn(currencies);
        given(currencyMapper.toDto(eq(currency))).willReturn(currencyDto);
        given(currencyMapper.toDto(eq(currency2))).willReturn(currencyDto2);
        given(currencyMapper.toDto(eq(currency3))).willReturn(currencyDto3);

        // when
        List<CurrencyDto> result = currencyService.getAllCurrencies();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCurrencyType()).isEqualTo(CurrencyType.USD);
        assertThat(result.get(1).getCurrencyType()).isEqualTo(CurrencyType.EUR);
        assertThat(result.get(2).getCurrencyType()).isEqualTo(CurrencyType.JPY);

        then(currencyRepository).should().findAll();
        then(currencyMapper).should().toDto(eq(currency));
        then(currencyMapper).should().toDto(eq(currency2));
        then(currencyMapper).should().toDto(eq(currency3));
    }

    @Test
    @DisplayName("전체 환율 목록 조회 성공 - 빈 목록")
    void getAllCurrencies_EmptyList() {
        // given
        given(currencyRepository.findAll()).willReturn(List.of());

        // when
        List<CurrencyDto> result = currencyService.getAllCurrencies();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        then(currencyRepository).should().findAll();
    }
}
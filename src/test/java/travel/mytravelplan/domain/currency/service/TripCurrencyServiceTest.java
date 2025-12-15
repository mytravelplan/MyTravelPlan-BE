package travel.mytravelplan.domain.currency.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.currency.dto.TripCurrencyCreateRequestDto;
import travel.mytravelplan.domain.currency.dto.TripCurrencyDto;
import travel.mytravelplan.domain.currency.dto.TripCurrencyUpdateRequestDto;
import travel.mytravelplan.domain.currency.entity.Currency;
import travel.mytravelplan.domain.currency.entity.TripCurrency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.exception.CurrencyException;
import travel.mytravelplan.domain.currency.exception.TripCurrencyException;
import travel.mytravelplan.domain.currency.repository.CurrencyRepository;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.mapper.TripCurrencyMapper;
import travel.mytravelplan.domain.trip.repository.TripRepository;
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

@DisplayName("여행 환율 서비스 테스트")
class TripCurrencyServiceTest extends ServiceTestSupport {

    @Mock
    private TripCurrencyRepository tripCurrencyRepository;

    @Mock
    private TripCurrencyMapper tripCurrencyMapper;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private TripCurrencyService tripCurrencyService;

    private Trip trip;
    private Currency currency;
    private TripCurrency tripCurrency;
    private TripCurrencyDto tripCurrencyDto;
    private TripCurrencyCreateRequestDto createRequestDto;
    private TripCurrencyUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        trip = Trip.createTrip("여행 제목", null, null, null, null);

        ReflectionTestUtils.setField(trip, "id", 1L);

        currency = Currency.createCurrency(CurrencyType.USD, new BigDecimal("1300.00"));

        tripCurrency = TripCurrency.createTripCurrency(trip, CurrencyType.USD, new BigDecimal("1300.00"));

        tripCurrencyDto = TripCurrencyDto.builder()
                .currencyType(CurrencyType.USD)
                .name("미국 달러")
                .exchangeRate(new BigDecimal("1300.00"))
                .build();

        createRequestDto = TripCurrencyCreateRequestDto.builder()
                .currencyType(CurrencyType.USD)
                .build();

        updateRequestDto = TripCurrencyUpdateRequestDto.builder()
                .exchangeRate(new BigDecimal("1350.00"))
                .build();
    }

    @Test
    @DisplayName("여행 환율 생성 성공")
    void createTripCurrency_Success() {
        // given
        given(tripRepository.findById(eq(1L))).willReturn(Optional.of(trip));
        given(currencyRepository.findByCurrencyType(eq(CurrencyType.USD))).willReturn(Optional.of(currency));
        given(tripCurrencyRepository.save(any(TripCurrency.class))).willReturn(tripCurrency);
        given(tripCurrencyMapper.toDto(any(TripCurrency.class))).willReturn(tripCurrencyDto);

        // when
        TripCurrencyDto result = tripCurrencyService.createTripCurrency(1L, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCurrencyType()).isEqualTo(CurrencyType.USD);
        assertThat(result.getName()).isEqualTo("미국 달러");
        assertThat(result.getExchangeRate()).isEqualTo(new BigDecimal("1300.00"));

        then(tripRepository).should().findById(eq(1L));
        then(currencyRepository).should().findByCurrencyType(eq(CurrencyType.USD));
        then(tripCurrencyRepository).should().save(any(TripCurrency.class));
        then(tripCurrencyMapper).should().toDto(any(TripCurrency.class));
    }

    @Test
    @DisplayName("여행 환율 생성 실패 - 존재하지 않는 여행")
    void createTripCurrency_TripNotFound() {
        // given
        given(tripRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripCurrencyService.createTripCurrency(1L, createRequestDto))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("여행 환율 생성 실패 - 존재하지 않는 환율")
    void createTripCurrency_CurrencyNotFound() {
        // given
        given(tripRepository.findById(eq(1L))).willReturn(Optional.of(trip));
        given(currencyRepository.findByCurrencyType(eq(CurrencyType.USD))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripCurrencyService.createTripCurrency(1L, createRequestDto))
                .isInstanceOf(CurrencyException.class);

        then(tripRepository).should().findById(eq(1L));
        then(currencyRepository).should().findByCurrencyType(eq(CurrencyType.USD));
    }

    @Test
    @DisplayName("여행 환율 목록 조회 성공")
    void getTripCurrencies_Success() {
        // given
        TripCurrency tripCurrency2 = TripCurrency.createTripCurrency(trip, CurrencyType.EUR, new BigDecimal("1400.00"));
        TripCurrency tripCurrency3 = TripCurrency.createTripCurrency(trip, CurrencyType.JPY, new BigDecimal("900.00"));

        TripCurrencyDto tripCurrencyDto2 = TripCurrencyDto.builder()
                .currencyType(CurrencyType.EUR)
                .name("유로")
                .exchangeRate(new BigDecimal("1400.00"))
                .build();

        TripCurrencyDto tripCurrencyDto3 = TripCurrencyDto.builder()
                .currencyType(CurrencyType.JPY)
                .name("일본 엔")
                .exchangeRate(new BigDecimal("900.00"))
                .build();

        List<TripCurrency> tripCurrencies = Arrays.asList(tripCurrency, tripCurrency2, tripCurrency3);

        given(tripRepository.findById(eq(1L))).willReturn(Optional.of(trip));
        given(tripCurrencyRepository.findByTripId(eq(1L))).willReturn(tripCurrencies);
        given(tripCurrencyMapper.toDto(eq(tripCurrency))).willReturn(tripCurrencyDto);
        given(tripCurrencyMapper.toDto(eq(tripCurrency2))).willReturn(tripCurrencyDto2);
        given(tripCurrencyMapper.toDto(eq(tripCurrency3))).willReturn(tripCurrencyDto3);

        // when
        List<TripCurrencyDto> result = tripCurrencyService.getTripCurrencies(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCurrencyType()).isEqualTo(CurrencyType.USD);
        assertThat(result.get(1).getCurrencyType()).isEqualTo(CurrencyType.EUR);
        assertThat(result.get(2).getCurrencyType()).isEqualTo(CurrencyType.JPY);

        then(tripRepository).should().findById(eq(1L));
        then(tripCurrencyRepository).should().findByTripId(eq(1L));
        then(tripCurrencyMapper).should().toDto(eq(tripCurrency));
        then(tripCurrencyMapper).should().toDto(eq(tripCurrency2));
        then(tripCurrencyMapper).should().toDto(eq(tripCurrency3));
    }

    @Test
    @DisplayName("여행 환율 목록 조회 실패 - 존재하지 않는 여행")
    void getTripCurrencies_TripNotFound() {
        // given
        given(tripRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripCurrencyService.getTripCurrencies(1L))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("여행 환율 목록 조회 성공 - 빈 목록")
    void getTripCurrencies_EmptyList() {
        // given
        given(tripRepository.findById(eq(1L))).willReturn(Optional.of(trip));
        given(tripCurrencyRepository.findByTripId(eq(1L))).willReturn(List.of());

        // when
        List<TripCurrencyDto> result = tripCurrencyService.getTripCurrencies(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        then(tripRepository).should().findById(eq(1L));
        then(tripCurrencyRepository).should().findByTripId(eq(1L));
    }

    @Test
    @DisplayName("여행 환율 수정 성공")
    void updateTripCurrency_Success() {
        // given
        TripCurrencyDto updatedDto = TripCurrencyDto.builder()
                .currencyType(CurrencyType.USD)
                .name("미국 달러")
                .exchangeRate(new BigDecimal("1350.00"))
                .build();

        given(tripRepository.findById(eq(1L))).willReturn(Optional.of(trip));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD))).willReturn(Optional.of(tripCurrency));
        given(tripCurrencyMapper.toDto(any(TripCurrency.class))).willReturn(updatedDto);

        // when
        TripCurrencyDto result = tripCurrencyService.updateTripCurrency(1L, CurrencyType.USD, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCurrencyType()).isEqualTo(CurrencyType.USD);
        assertThat(result.getExchangeRate()).isEqualTo(new BigDecimal("1350.00"));

        then(tripRepository).should().findById(eq(1L));
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
        then(tripCurrencyMapper).should().toDto(any(TripCurrency.class));
    }

    @Test
    @DisplayName("여행 환율 수정 실패 - 존재하지 않는 여행")
    void updateTripCurrency_TripNotFound() {
        // given
        given(tripRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripCurrencyService.updateTripCurrency(1L, CurrencyType.USD, updateRequestDto))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("여행 환율 수정 실패 - 존재하지 않는 여행 환율")
    void updateTripCurrency_TripCurrencyNotFound() {
        // given
        given(tripRepository.findById(eq(1L))).willReturn(Optional.of(trip));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripCurrencyService.updateTripCurrency(1L, CurrencyType.USD, updateRequestDto))
                .isInstanceOf(TripCurrencyException.class);

        then(tripRepository).should().findById(eq(1L));
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
    }
}
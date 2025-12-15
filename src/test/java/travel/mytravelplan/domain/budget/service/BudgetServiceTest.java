package travel.mytravelplan.domain.budget.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.budget.dto.*;
import travel.mytravelplan.domain.budget.entity.Budget;
import travel.mytravelplan.domain.budget.entity.BudgetParticipant;
import travel.mytravelplan.domain.budget.entity.PersonalBudget;
import travel.mytravelplan.domain.budget.entity.SharedBudget;
import travel.mytravelplan.domain.budget.exception.BudgetException;
import travel.mytravelplan.domain.budget.mapper.BudgetMapper;
import travel.mytravelplan.domain.budget.repository.BudgetParticipantRepository;
import travel.mytravelplan.domain.budget.repository.BudgetRepository;
import travel.mytravelplan.domain.currency.entity.TripCurrency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.exception.CurrencyException;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("예산 서비스 테스트")
class BudgetServiceTest extends ServiceTestSupport {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetParticipantRepository budgetParticipantRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripJoinRepository tripJoinRepository;

    @Mock
    private TripCurrencyRepository tripCurrencyRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetService budgetService;

    private Trip trip;
    private User user1;
    private User user2;
    private TripJoin tripJoin1;
    private TripJoin tripJoin2;
    private PersonalBudget personalBudget;
    private SharedBudget sharedBudget;
    private PersonalBudgetDto personalBudgetDto;
    private SharedBudgetDto sharedBudgetDto;
    private TripCurrency tripCurrency;

    @BeforeEach
    void setUp() {
        trip = Trip.createTrip(
                "테스트 여행",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 10),
                "https://example.com/image.jpg",
                new java.util.HashSet<>()
        );
        ReflectionTestUtils.setField(trip, "id", 1L);

        user1 = User.createUser(
                "user1",
                "password1",
                "user1@example.com",
                null,
                null,
                new java.util.HashSet<>()
        );
        ReflectionTestUtils.setField(user1, "id", 1L);

        user2 = User.createUser(
                "user2",
                "password2",
                "user2@example.com",
                null,
                null,
                new java.util.HashSet<>()
        );
        ReflectionTestUtils.setField(user2, "id", 2L);

        tripJoin1 = TripJoin.createTripJoin(trip, user1);
        tripJoin2 = TripJoin.createTripJoin(trip, user2);

        personalBudget = PersonalBudget.createPersonalBudget(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "개인 예산",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(100000),
                trip
        );

        List<BudgetParticipant> participants = Arrays.asList(
                BudgetParticipant.createBudgetParticipant(tripJoin1, BigDecimal.valueOf(50000)),
                BudgetParticipant.createBudgetParticipant(tripJoin2, BigDecimal.valueOf(50000))
        );

        sharedBudget = SharedBudget.createSharedBudget(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "공유 예산",
                PaymentMethod.CARD,
                CurrencyType.USD,
                BigDecimal.valueOf(1300),
                CalculateType.EQUAL,
                participants,
                trip
        );

        tripCurrency = TripCurrency.createTripCurrency(trip, CurrencyType.KRW, BigDecimal.valueOf(1300));

        personalBudgetDto = PersonalBudgetDto.builder()
                .id(1L)
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1200))
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        sharedBudgetDto = SharedBudgetDto.builder()
                .id(2L)
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EQUAL)
                .build();
    }

    @Test
    @DisplayName("개인 예산 생성 성공")
    void createPersonalBudget_Success() {
        // given
        Long tripId = 1L;
        PersonalBudgetCreateRequestDto requestDto = PersonalBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("개인 예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.KRW)
                .exchangeRate(BigDecimal.valueOf(1200))
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.save(any(PersonalBudget.class))).willReturn(personalBudget);
        given(budgetMapper.toDto(any(PersonalBudget.class))).willReturn(personalBudgetDto);

        // when
        BudgetDto result = budgetService.createBudget(tripId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(personalBudgetDto);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().save(any(PersonalBudget.class));
        then(budgetMapper).should().toDto(any(PersonalBudget.class));
    }

    @Test
    @DisplayName("개인 예산 생성 성공 - 환율 자동 조회")
    void createPersonalBudget_WithAutoExchangeRate() {
        // given
        Long tripId = 1L;
        PersonalBudgetCreateRequestDto requestDto = PersonalBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("개인 예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.KRW)
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.KRW)))
                .willReturn(Optional.of(tripCurrency));
        given(budgetRepository.save(any(PersonalBudget.class))).willReturn(personalBudget);
        given(budgetMapper.toDto(any(PersonalBudget.class))).willReturn(personalBudgetDto);

        // when
        BudgetDto result = budgetService.createBudget(tripId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.KRW));
        then(budgetRepository).should().save(any(PersonalBudget.class));
        then(budgetMapper).should().toDto(any(PersonalBudget.class));
    }

    @Test
    @DisplayName("공유 예산 생성 성공 - EQUAL 타입")
    void createSharedBudget_EqualType_Success() {
        // given
        Long tripId = 1L;
        SharedBudgetCreateRequestDto requestDto = SharedBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(50000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(50000)).build()
                ))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1, tripJoin2));
        given(budgetRepository.save(any(SharedBudget.class))).willReturn(sharedBudget);
        given(budgetMapper.toDto(any(SharedBudget.class))).willReturn(sharedBudgetDto);

        // when
        BudgetDto result = budgetService.createBudget(tripId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
        then(budgetRepository).should().save(any(SharedBudget.class));
        then(budgetParticipantRepository).should().saveAll(anyList());
        then(budgetMapper).should().toDto(any(SharedBudget.class));
    }

    @Test
    @DisplayName("공유 예산 생성 성공 - EACH 타입")
    void createSharedBudget_EachType_Success() {
        // given
        Long tripId = 1L;
        SharedBudgetCreateRequestDto requestDto = SharedBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EACH)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(30000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(70000)).build()
                ))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1, tripJoin2));
        given(budgetRepository.save(any(SharedBudget.class))).willReturn(sharedBudget);
        given(budgetMapper.toDto(any(SharedBudget.class))).willReturn(sharedBudgetDto);

        // when
        BudgetDto result = budgetService.createBudget(tripId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
        then(budgetRepository).should().save(any(SharedBudget.class));
        then(budgetParticipantRepository).should().saveAll(anyList());
        then(budgetMapper).should().toDto(any(SharedBudget.class));
    }

    @Test
    @DisplayName("예산 생성 실패 - 여행을 찾을 수 없음")
    void createBudget_TripNotFound() {
        // given
        Long tripId = 999L;
        PersonalBudgetCreateRequestDto requestDto = PersonalBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("개인 예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.KRW)
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.createBudget(tripId, requestDto))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("공유 예산 생성 실패 - 환율 정보를 찾을 수 없음")
    void createSharedBudget_CurrencyNotFound() {
        // given
        Long tripId = 1L;
        SharedBudgetCreateRequestDto requestDto = SharedBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(50000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(50000)).build()
                ))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1, tripJoin2));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.createBudget(tripId, requestDto))
                .isInstanceOf(CurrencyException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
    }

    @Test
    @DisplayName("공유 예산 생성 실패 - 중복된 참가자")
    void createSharedBudget_DuplicateParticipants() {
        // given
        Long tripId = 1L;
        SharedBudgetCreateRequestDto requestDto = SharedBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(50000)).build(),
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(50000)).build()
                ))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> budgetService.createBudget(tripId, requestDto))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("공유 예산 생성 실패 - 잘못된 정산 방식")
    void createSharedBudget_InvalidCalculateType() {
        // given
        Long tripId = 1L;
        SharedBudgetCreateRequestDto requestDto = SharedBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(null) // null로 설정하여 EQUAL도 EACH도 아닌 경우
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(50000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(50000)).build()
                ))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> budgetService.createBudget(tripId, requestDto))
                .isInstanceOf(BudgetException.class)
                .hasMessageContaining("정산 방식");

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("예산 생성 실패 - 잘못된 예산 타입")
    void createBudget_InvalidBudgetType() {
        // given
        Long tripId = 1L;
        BudgetCreateRequestDto requestDto = new BudgetCreateRequestDto() {
            @Override
            public travel.mytravelplan.domain.budget.enums.BudgetType getBudgetType() {
                return null; // null로 설정하여 PERSONAL도 SHARED도 아닌 경우
            }

            @Override
            public LocalDateTime getDateTime() {
                return LocalDateTime.of(2024, 1, 1, 12, 0);
            }

            @Override
            public String getMemo() {
                return "테스트 예산";
            }

            @Override
            public PaymentMethod getPaymentMethod() {
                return PaymentMethod.CARD;
            }

            @Override
            public CurrencyType getCurrencyType() {
                return CurrencyType.KRW;
            }

            @Override
            public BigDecimal getExchangeRate() {
                return BigDecimal.valueOf(1);
            }
        };

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> budgetService.createBudget(tripId, requestDto))
                .isInstanceOf(BudgetException.class)
                .hasMessageContaining("유형");

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("예산 조회 성공")
    void getBudget_Success() {
        // given
        Long tripId = 1L;
        Long budgetId = 1L;

        ReflectionTestUtils.setField(personalBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(personalBudget));
        given(budgetMapper.toDto(eq(personalBudget))).willReturn(personalBudgetDto);

        // when
        BudgetDto result = budgetService.getBudget(tripId, budgetId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(personalBudgetDto);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(budgetMapper).should().toDto(eq(personalBudget));
    }

    @Test
    @DisplayName("예산 조회 실패 - 예산을 찾을 수 없음")
    void getBudget_BudgetNotFound() {
        // given
        Long tripId = 1L;
        Long budgetId = 999L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.getBudget(tripId, budgetId))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
    }

    @Test
    @DisplayName("예산 조회 실패 - 예산이 여행에 속하지 않음")
    void getBudget_BudgetNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long budgetId = 1L;
        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/another.jpg",
                new java.util.HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        PersonalBudget anotherBudget = PersonalBudget.createPersonalBudget(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "다른 여행 예산",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(100000),
                anotherTrip
        );
        ReflectionTestUtils.setField(anotherBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(anotherBudget));

        // when & then
        assertThatThrownBy(() -> budgetService.getBudget(tripId, budgetId))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
    }

    @Test
    @DisplayName("예산 목록 조회 성공")
    void getBudgets_Success() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Budget> budgets = Arrays.asList(personalBudget, sharedBudget);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(budgets);
        given(budgetMapper.toDto(eq(personalBudget))).willReturn(personalBudgetDto);
        given(budgetMapper.toDto(eq(sharedBudget))).willReturn(sharedBudgetDto);

        // when
        CursorPageResponseDto<BudgetDto> result = budgetService.getBudgets(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(budgetMapper).should().toDto(eq(personalBudget));
        then(budgetMapper).should().toDto(eq(sharedBudget));
    }

    @Test
    @DisplayName("예산 목록 조회 성공 - hasNext true")
    void getBudgets_HasNext() {
        // given
        Long tripId = 1L;
        int limit = 2;

        PersonalBudget budget1 = PersonalBudget.createPersonalBudget(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "예산1",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(100000),
                trip
        );
        PersonalBudget budget2 = PersonalBudget.createPersonalBudget(
                LocalDateTime.of(2024, 1, 2, 12, 0),
                "예산2",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(100000),
                trip
        );
        PersonalBudget budget3 = PersonalBudget.createPersonalBudget(
                LocalDateTime.of(2024, 1, 3, 12, 0),
                "예산3",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(100000),
                trip
        );

        ReflectionTestUtils.setField(budget1, "id", 1L);
        ReflectionTestUtils.setField(budget1, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0));
        ReflectionTestUtils.setField(budget2, "id", 2L);
        ReflectionTestUtils.setField(budget2, "createdAt", LocalDateTime.of(2024, 1, 2, 12, 0));
        ReflectionTestUtils.setField(budget3, "id", 3L);
        ReflectionTestUtils.setField(budget3, "createdAt", LocalDateTime.of(2024, 1, 3, 12, 0));

        List<Budget> budgets = Arrays.asList(budget1, budget2, budget3);

        PersonalBudgetDto budgetDto1 = PersonalBudgetDto.builder().id(1L).dateTime(LocalDateTime.of(2024, 1, 1, 12, 0)).memo("예산1").paymentMethod(PaymentMethod.CASH).currencyType(CurrencyType.KRW).exchangeRate(BigDecimal.valueOf(1200)).totalAmount(BigDecimal.valueOf(100000)).build();
        PersonalBudgetDto budgetDto2 = PersonalBudgetDto.builder().id(2L).dateTime(LocalDateTime.of(2024, 1, 2, 12, 0)).memo("예산2").paymentMethod(PaymentMethod.CASH).currencyType(CurrencyType.KRW).exchangeRate(BigDecimal.valueOf(1200)).totalAmount(BigDecimal.valueOf(100000)).build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findAllByCursor(eq(tripId), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(limit + 1)))
                .willReturn(budgets);
        given(budgetMapper.toDto(eq(budget1))).willReturn(budgetDto1);
        given(budgetMapper.toDto(eq(budget2))).willReturn(budgetDto2);

        // when
        CursorPageResponseDto<BudgetDto> result = budgetService.getBudgets(tripId, null, "createdAt", "desc", null, null, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findAllByCursor(eq(tripId), eq(null), eq("createdAt"), eq("desc"), eq(null), eq(null), eq(limit + 1));
    }

    @Test
    @DisplayName("개인 예산 수정 성공")
    void updatePersonalBudget_Success() {
        // given
        Long tripId = 1L;
        Long budgetId = 1L;

        PersonalBudgetUpdateRequestDto requestDto = PersonalBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 개인 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        ReflectionTestUtils.setField(personalBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(personalBudget));
        given(budgetMapper.toDto(eq(personalBudget))).willReturn(personalBudgetDto);

        // when
        BudgetDto result = budgetService.updateBudget(tripId, budgetId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(budgetMapper).should().toDto(eq(personalBudget));
    }

    @Test
    @DisplayName("공유 예산 수정 성공")
    void updateSharedBudget_Success() {
        // given
        Long tripId = 1L;
        Long budgetId = 2L;

        SharedBudgetUpdateRequestDto requestDto = SharedBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(60000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(60000)).build()
                ))
                .build();

        ReflectionTestUtils.setField(sharedBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(sharedBudget));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1, tripJoin2));
        given(budgetMapper.toDto(eq(sharedBudget))).willReturn(sharedBudgetDto);

        // when
        BudgetDto result = budgetService.updateBudget(tripId, budgetId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
        then(budgetMapper).should().toDto(eq(sharedBudget));
    }

    @Test
    @DisplayName("예산 수정 실패 - 예산을 찾을 수 없음")
    void updateBudget_BudgetNotFound() {
        // given
        Long tripId = 1L;
        Long budgetId = 999L;

        PersonalBudgetUpdateRequestDto requestDto = PersonalBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 개인 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.updateBudget(tripId, budgetId, requestDto))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
    }

    @Test
    @DisplayName("예산 삭제 성공")
    void deleteBudget_Success() {
        // given
        Long tripId = 1L;
        Long budgetId = 1L;

        ReflectionTestUtils.setField(personalBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(personalBudget));

        // when
        budgetService.deleteBudget(tripId, budgetId);

        // then
        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(budgetRepository).should().delete(eq(personalBudget));
    }

    @Test
    @DisplayName("예산 삭제 실패 - 예산을 찾을 수 없음")
    void deleteBudget_BudgetNotFound() {
        // given
        Long tripId = 1L;
        Long budgetId = 999L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.deleteBudget(tripId, budgetId))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
    }

    @Test
    @DisplayName("예산 삭제 실패 - 예산이 여행에 속하지 않음")
    void deleteBudget_BudgetNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long budgetId = 1L;
        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/another.jpg",
                new java.util.HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        PersonalBudget anotherBudget = PersonalBudget.createPersonalBudget(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "다른 여행 예산",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(100000),
                anotherTrip
        );
        ReflectionTestUtils.setField(anotherBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(anotherBudget));

        // when & then
        assertThatThrownBy(() -> budgetService.deleteBudget(tripId, budgetId))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
    }

    @Test
    @DisplayName("개인 예산 생성 실패 - 환율 자동 조회 시 통화를 찾을 수 없음")
    void createPersonalBudget_AutoExchangeRate_CurrencyNotFound() {
        // given
        Long tripId = 1L;
        PersonalBudgetCreateRequestDto requestDto = PersonalBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("개인 예산")
                .paymentMethod(PaymentMethod.CASH)
                .currencyType(CurrencyType.USD)
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.createBudget(tripId, requestDto))
                .isInstanceOf(CurrencyException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
    }

    @Test
    @DisplayName("공유 예산 생성 성공 - 환율 자동 조회")
    void createSharedBudget_WithAutoExchangeRate() {
        // given
        Long tripId = 1L;
        SharedBudgetCreateRequestDto requestDto = SharedBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(50000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(50000)).build()
                ))
                .build();

        TripCurrency usdCurrency = TripCurrency.createTripCurrency(trip, CurrencyType.USD, BigDecimal.valueOf(1300));

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1, tripJoin2));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD)))
                .willReturn(Optional.of(usdCurrency));
        given(budgetRepository.save(any(SharedBudget.class))).willReturn(sharedBudget);
        given(budgetMapper.toDto(any(SharedBudget.class))).willReturn(sharedBudgetDto);

        // when
        BudgetDto result = budgetService.createBudget(tripId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
        then(budgetRepository).should().save(any(SharedBudget.class));
        then(budgetParticipantRepository).should().saveAll(anyList());
        then(budgetMapper).should().toDto(any(SharedBudget.class));
    }

    @Test
    @DisplayName("공유 예산 생성 실패 - 참가자를 찾을 수 없음")
    void createSharedBudget_ParticipantNotFound() {
        // given
        Long tripId = 1L;
        SharedBudgetCreateRequestDto requestDto = SharedBudgetCreateRequestDto.builder()
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(50000)).build(),
                        BudgetParticipantRequestDto.builder().id(999L).amount(BigDecimal.valueOf(50000)).build()
                ))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1));

        // when & then
        assertThatThrownBy(() -> budgetService.createBudget(tripId, requestDto))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
    }

    @Test
    @DisplayName("개인 예산 수정 성공 - 환율 자동 조회")
    void updatePersonalBudget_WithAutoExchangeRate() {
        // given
        Long tripId = 1L;
        Long budgetId = 1L;

        PersonalBudgetUpdateRequestDto requestDto = PersonalBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 개인 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        TripCurrency usdCurrency = TripCurrency.createTripCurrency(trip, CurrencyType.USD, BigDecimal.valueOf(1300));

        ReflectionTestUtils.setField(personalBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(personalBudget));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD)))
                .willReturn(Optional.of(usdCurrency));
        given(budgetMapper.toDto(eq(personalBudget))).willReturn(personalBudgetDto);

        // when
        BudgetDto result = budgetService.updateBudget(tripId, budgetId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
        then(budgetMapper).should().toDto(eq(personalBudget));
    }

    @Test
    @DisplayName("개인 예산 수정 실패 - 환율 자동 조회 시 통화를 찾을 수 없음")
    void updatePersonalBudget_AutoExchangeRate_CurrencyNotFound() {
        // given
        Long tripId = 1L;
        Long budgetId = 1L;

        PersonalBudgetUpdateRequestDto requestDto = PersonalBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 개인 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        ReflectionTestUtils.setField(personalBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(personalBudget));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.updateBudget(tripId, budgetId, requestDto))
                .isInstanceOf(CurrencyException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
    }

    @Test
    @DisplayName("공유 예산 수정 성공 - 환율 자동 조회")
    void updateSharedBudget_WithAutoExchangeRate() {
        // given
        Long tripId = 1L;
        Long budgetId = 2L;

        SharedBudgetUpdateRequestDto requestDto = SharedBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(60000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(60000)).build()
                ))
                .build();

        TripCurrency usdCurrency = TripCurrency.createTripCurrency(trip, CurrencyType.USD, BigDecimal.valueOf(1300));

        ReflectionTestUtils.setField(sharedBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(sharedBudget));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1, tripJoin2));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD)))
                .willReturn(Optional.of(usdCurrency));
        given(budgetMapper.toDto(eq(sharedBudget))).willReturn(sharedBudgetDto);

        // when
        BudgetDto result = budgetService.updateBudget(tripId, budgetId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
        then(budgetMapper).should().toDto(eq(sharedBudget));
    }

    @Test
    @DisplayName("공유 예산 수정 실패 - 환율 자동 조회 시 통화를 찾을 수 없음")
    void updateSharedBudget_AutoExchangeRate_CurrencyNotFound() {
        // given
        Long tripId = 1L;
        Long budgetId = 2L;

        SharedBudgetUpdateRequestDto requestDto = SharedBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(60000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(60000)).build()
                ))
                .build();

        ReflectionTestUtils.setField(sharedBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(sharedBudget));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1, tripJoin2));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.updateBudget(tripId, budgetId, requestDto))
                .isInstanceOf(CurrencyException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
        then(tripCurrencyRepository).should().findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
    }

    @Test
    @DisplayName("공유 예산 수정 실패 - 예산이 여행에 속하지 않음")
    void updateSharedBudget_BudgetNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long budgetId = 2L;
        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/another.jpg",
                new java.util.HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        List<BudgetParticipant> participants = Arrays.asList(
                BudgetParticipant.createBudgetParticipant(tripJoin1, BigDecimal.valueOf(50000)),
                BudgetParticipant.createBudgetParticipant(tripJoin2, BigDecimal.valueOf(50000))
        );

        SharedBudget anotherBudget = SharedBudget.createSharedBudget(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "다른 여행 공유 예산",
                PaymentMethod.CARD,
                CurrencyType.USD,
                BigDecimal.valueOf(1300),
                CalculateType.EQUAL,
                participants,
                anotherTrip
        );
        ReflectionTestUtils.setField(anotherBudget, "id", budgetId);

        SharedBudgetUpdateRequestDto requestDto = SharedBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(60000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(60000)).build()
                ))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(anotherBudget));

        // when & then
        assertThatThrownBy(() -> budgetService.updateBudget(tripId, budgetId, requestDto))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
    }

    @Test
    @DisplayName("공유 예산 수정 실패 - 중복된 참가자")
    void updateSharedBudget_DuplicateParticipants() {
        // given
        Long tripId = 1L;
        Long budgetId = 2L;

        SharedBudgetUpdateRequestDto requestDto = SharedBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(60000)).build(),
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(60000)).build()
                ))
                .build();

        ReflectionTestUtils.setField(sharedBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(sharedBudget));

        // when & then
        assertThatThrownBy(() -> budgetService.updateBudget(tripId, budgetId, requestDto))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
    }

    @Test
    @DisplayName("공유 예산 수정 실패 - 참가자를 찾을 수 없음")
    void updateSharedBudget_ParticipantNotFound() {
        // given
        Long tripId = 1L;
        Long budgetId = 2L;

        SharedBudgetUpdateRequestDto requestDto = SharedBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EQUAL)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(60000)).build(),
                        BudgetParticipantRequestDto.builder().id(999L).amount(BigDecimal.valueOf(60000)).build()
                ))
                .build();

        ReflectionTestUtils.setField(sharedBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(sharedBudget));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1));

        // when & then
        assertThatThrownBy(() -> budgetService.updateBudget(tripId, budgetId, requestDto))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
    }

    @Test
    @DisplayName("공유 예산 수정 성공 - EACH 타입")
    void updateSharedBudget_EachType_Success() {
        // given
        Long tripId = 1L;
        Long budgetId = 2L;

        SharedBudgetUpdateRequestDto requestDto = SharedBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 공유 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .calculateType(CalculateType.EACH)
                .budgetParticipants(Arrays.asList(
                        BudgetParticipantRequestDto.builder().id(1L).amount(BigDecimal.valueOf(30000)).build(),
                        BudgetParticipantRequestDto.builder().id(2L).amount(BigDecimal.valueOf(70000)).build()
                ))
                .build();

        ReflectionTestUtils.setField(sharedBudget, "id", budgetId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(sharedBudget));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList()))
                .willReturn(Arrays.asList(tripJoin1, tripJoin2));
        given(budgetMapper.toDto(eq(sharedBudget))).willReturn(sharedBudgetDto);

        // when
        BudgetDto result = budgetService.updateBudget(tripId, budgetId, requestDto);

        // then
        assertThat(result).isNotNull();

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
        then(tripJoinRepository).should().findByTripIdAndUserIdIn(eq(tripId), anyList());
        then(budgetMapper).should().toDto(eq(sharedBudget));
    }

    @Test
    @DisplayName("개인 예산 수정 실패 - 예산이 여행에 속하지 않음")
    void updatePersonalBudget_BudgetNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long budgetId = 1L;
        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/another.jpg",
                new java.util.HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        PersonalBudget anotherBudget = PersonalBudget.createPersonalBudget(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "다른 여행 예산",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(100000),
                anotherTrip
        );
        ReflectionTestUtils.setField(anotherBudget, "id", budgetId);

        PersonalBudgetUpdateRequestDto requestDto = PersonalBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 개인 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findById(eq(budgetId))).willReturn(Optional.of(anotherBudget));

        // when & then
        assertThatThrownBy(() -> budgetService.updateBudget(tripId, budgetId, requestDto))
                .isInstanceOf(BudgetException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findById(eq(budgetId));
    }

    @Test
    @DisplayName("예산 목록 조회 실패 - 여행을 찾을 수 없음")
    void getBudgets_TripNotFound() {
        // given
        Long tripId = 999L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.getBudgets(tripId, null, "createdAt", "desc", null, null, 10))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("예산 목록 조회 성공 - 키워드 검색")
    void getBudgets_WithKeyword() {
        // given
        Long tripId = 1L;
        String keyword = "개인";
        String orderBy = "createdAt";
        String direction = "desc";
        int limit = 10;

        List<Budget> budgets = Arrays.asList(personalBudget);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(null), eq(null), eq(limit + 1)))
                .willReturn(budgets);
        given(budgetMapper.toDto(eq(personalBudget))).willReturn(personalBudgetDto);

        // when
        CursorPageResponseDto<BudgetDto> result = budgetService.getBudgets(tripId, keyword, orderBy, direction, null, null, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(null), eq(null), eq(limit + 1));
        then(budgetMapper).should().toDto(eq(personalBudget));
    }

    @Test
    @DisplayName("예산 목록 조회 성공 - 커서 페이징")
    void getBudgets_WithCursor() {
        // given
        Long tripId = 1L;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = "2024-01-01T12:00:00";
        Long after = 1L;
        int limit = 10;

        List<Budget> budgets = Arrays.asList(sharedBudget);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(budgetRepository.findAllByCursor(eq(tripId), eq(null), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(budgets);
        given(budgetMapper.toDto(eq(sharedBudget))).willReturn(sharedBudgetDto);

        // when
        CursorPageResponseDto<BudgetDto> result = budgetService.getBudgets(tripId, null, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(tripRepository).should().findById(eq(tripId));
        then(budgetRepository).should().findAllByCursor(eq(tripId), eq(null), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(budgetMapper).should().toDto(eq(sharedBudget));
    }

    @Test
    @DisplayName("예산 조회 실패 - 여행을 찾을 수 없음")
    void getBudget_TripNotFound() {
        // given
        Long tripId = 999L;
        Long budgetId = 1L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.getBudget(tripId, budgetId))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("예산 수정 실패 - 여행을 찾을 수 없음")
    void updateBudget_TripNotFound() {
        // given
        Long tripId = 999L;
        Long budgetId = 1L;

        PersonalBudgetUpdateRequestDto requestDto = PersonalBudgetUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .memo("수정된 개인 예산")
                .paymentMethod(PaymentMethod.CARD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(BigDecimal.valueOf(1300))
                .totalAmount(BigDecimal.valueOf(200000))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.updateBudget(tripId, budgetId, requestDto))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("예산 삭제 실패 - 여행을 찾을 수 없음")
    void deleteBudget_TripNotFound() {
        // given
        Long tripId = 999L;
        Long budgetId = 1L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.deleteBudget(tripId, budgetId))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }
}


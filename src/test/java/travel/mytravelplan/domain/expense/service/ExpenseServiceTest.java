package travel.mytravelplan.domain.expense.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.budget.entity.BudgetParticipant;
import travel.mytravelplan.domain.budget.entity.SharedBudget;
import travel.mytravelplan.domain.budget.repository.BudgetRepository;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.exception.TripCurrencyException;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.expense.dto.*;
import travel.mytravelplan.domain.expense.entity.*;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.GroupByType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.expense.exception.ExpenseException;
import travel.mytravelplan.domain.expense.mapper.ExpenseMapper;
import travel.mytravelplan.domain.expense.repository.ExpenseParticipantRepository;
import travel.mytravelplan.domain.expense.repository.ExpenseRepository;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.schedule.exception.ScheduleException;
import travel.mytravelplan.domain.schedule.repository.ScheduleRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@DisplayName("지출 서비스 테스트")
class ExpenseServiceTest extends ServiceTestSupport {

    @InjectMocks
    private ExpenseService expenseService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseParticipantRepository expenseParticipantRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private TripJoinRepository tripJoinRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private TripCurrencyRepository tripCurrencyRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TripRepository tripRepository;

    private Trip trip;
    private Schedule schedule;
    private TripJoin tripJoin;

    @BeforeEach
    void setUp() {
        // Given - 공통 테스트 데이터 설정
        trip = Trip.createTrip(
                "테스트 여행",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 10),
                "https://example.com/image.jpg",
                new HashSet<>()
        );
        ReflectionTestUtils.setField(trip, "id", 1L);

        User user = User.createUser(
                "testuser",
                "password",
                "testuser@example.com",
                null,
                null,
                new HashSet<>()
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        UserProfile userProfile = UserProfile.createUserProfile("테스트유저", "https://example.com/profile.jpg");

        ReflectionTestUtils.setField(userProfile, "id", 1L);

        user.setUserProfile(userProfile);

        schedule = Schedule.createSchedule(
                "첫날 일정",
                LocalDateTime.of(2024, 1, 1, 9, 0),
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "메모",
                1L,
                null,
                trip,
                null
        );
        ReflectionTestUtils.setField(schedule, "id", 1L);

        tripJoin = TripJoin.createTripJoin(trip, user);
        ReflectionTestUtils.setField(tripJoin, "id", 1L);
    }

    @Test
    @DisplayName("개인 지출을 생성할 수 있다")
    void createPersonalExpense() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;

        PersonalExpenseCreateRequestDto requestDto = PersonalExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.PERSONAL)
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("점심 식사")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .totalAmount(new BigDecimal("50"))
                .build();

        PersonalExpense personalExpense = PersonalExpense.createPersonalExpense(
                requestDto.getDateTime(),
                requestDto.getMemo(),
                requestDto.getPaymentMethod(),
                requestDto.getCategory(),
                requestDto.getCurrencyType(),
                requestDto.getExchangeRate(),
                requestDto.getTotalAmount(),
                schedule
        );
        ReflectionTestUtils.setField(personalExpense, "id", 1L);

        PersonalExpenseDto expectedDto = PersonalExpenseDto.builder()
                .id(1L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.save(any(PersonalExpense.class))).willReturn(personalExpense);
        given(expenseMapper.toDto(any(PersonalExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.createExpense(tripId, scheduleId, requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PersonalExpenseDto.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).save(any(PersonalExpense.class));
        verify(expenseMapper).toDto(any(PersonalExpense.class));
    }

    @Test
    @DisplayName("공동 지출을 균등 분담으로 생성할 수 있다")
    void createSharedExpenseWithEqualCalculateType() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long payerId = 1L;

        User user2 = User.createUser(
                "user2",
                "password2",
                "user2@example.com",
                null,
                null,
                new HashSet<>()
        );
        ReflectionTestUtils.setField(user2, "id", 2L);

        UserProfile userProfile2 = UserProfile.createUserProfile("유저2", "https://example.com/profile2.jpg");
        ReflectionTestUtils.setField(user2, "userProfile", userProfile2);

        TripJoin tripJoin2 = TripJoin.createTripJoin(trip, user2);
        ReflectionTestUtils.setField(tripJoin2, "id", 2L);

        List<ExpenseParticipantRequestDto> participants = Arrays.asList(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build(),
                ExpenseParticipantRequestDto.builder()
                        .id(2L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseCreateRequestDto requestDto = SharedExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.SHARED)
                .dateTime(LocalDateTime.of(2024, 1, 1, 18, 0))
                .memo("저녁 식사")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                requestDto.getDateTime(),
                requestDto.getMemo(),
                requestDto.getPaymentMethod(),
                requestDto.getCategory(),
                requestDto.getCurrencyType(),
                requestDto.getExchangeRate(),
                requestDto.getCalculateType(),
                tripJoin,
                new ArrayList<>(),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", 1L);

        SharedExpenseDto expectedDto = SharedExpenseDto.builder()
                .id(1L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList())).willReturn(Arrays.asList(tripJoin, tripJoin2));
        given(expenseRepository.save(any(SharedExpense.class))).willReturn(sharedExpense);
        given(expenseParticipantRepository.saveAll(anyList())).willReturn(new ArrayList<>());
        given(expenseMapper.toDto(any(SharedExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.createExpense(tripId, scheduleId, requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SharedExpenseDto.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(tripJoinRepository).findByUserIdAndTripId(eq(payerId), eq(tripId));
        verify(tripJoinRepository).findByTripIdAndUserIdIn(eq(tripId), anyList());
        verify(expenseRepository).save(any(SharedExpense.class));
        verify(expenseParticipantRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("공동 지출을 개별 금액으로 생성할 수 있다")
    void createSharedExpenseWithEachCalculateType() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long payerId = 1L;

        User user2 = User.createUser(
                "user2",
                "password2",
                "user2@example.com",
                null,
                null,
                new HashSet<>()
        );
        ReflectionTestUtils.setField(user2, "id", 2L);

        UserProfile userProfile2 = UserProfile.createUserProfile("유저2", "https://example.com/profile2.jpg");
        ReflectionTestUtils.setField(user2, "userProfile", userProfile2);

        TripJoin tripJoin2 = TripJoin.createTripJoin(trip, user2);
        ReflectionTestUtils.setField(tripJoin2, "id", 2L);

        List<ExpenseParticipantRequestDto> participants = Arrays.asList(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("60"))
                        .build(),
                ExpenseParticipantRequestDto.builder()
                        .id(2L)
                        .amount(new BigDecimal("40"))
                        .build()
        );

        SharedExpenseCreateRequestDto requestDto = SharedExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.SHARED)
                .dateTime(LocalDateTime.of(2024, 1, 1, 18, 0))
                .memo("저녁 식사")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .calculateType(CalculateType.EACH)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                requestDto.getDateTime(),
                requestDto.getMemo(),
                requestDto.getPaymentMethod(),
                requestDto.getCategory(),
                requestDto.getCurrencyType(),
                requestDto.getExchangeRate(),
                requestDto.getCalculateType(),
                tripJoin,
                new ArrayList<>(),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", 1L);

        SharedExpenseDto expectedDto = SharedExpenseDto.builder()
                .id(1L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList())).willReturn(Arrays.asList(tripJoin, tripJoin2));
        given(expenseRepository.save(any(SharedExpense.class))).willReturn(sharedExpense);
        given(expenseParticipantRepository.saveAll(anyList())).willReturn(new ArrayList<>());
        given(expenseMapper.toDto(any(SharedExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.createExpense(tripId, scheduleId, requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SharedExpenseDto.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(tripJoinRepository).findByUserIdAndTripId(eq(payerId), eq(tripId));
        verify(tripJoinRepository).findByTripIdAndUserIdIn(eq(tripId), anyList());
        verify(expenseRepository).save(any(SharedExpense.class));
        verify(expenseParticipantRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("여행이 존재하지 않으면 지출 생성 시 예외가 발생한다")
    void createExpenseThrowsExceptionWhenTripNotFound() {
        // Given
        Long tripId = 999L;
        Long scheduleId = 1L;

        PersonalExpenseCreateRequestDto requestDto = PersonalExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.PERSONAL)
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("점심 식사")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .totalAmount(new BigDecimal("50"))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(TripException.class);
        verify(tripRepository).findById(eq(tripId));
    }

    @Test
    @DisplayName("일정이 존재하지 않으면 지출 생성 시 예외가 발생한다")
    void createExpenseThrowsExceptionWhenScheduleNotFound() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 999L;

        PersonalExpenseCreateRequestDto requestDto = PersonalExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.PERSONAL)
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("점심 식사")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .totalAmount(new BigDecimal("50"))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(ScheduleException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
    }

    @Test
    @DisplayName("지출 정보를 조회할 수 있다")
    void getExpense() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        PersonalExpense expense = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(expense, "id", expenseId);

        PersonalExpenseDto expectedDto = PersonalExpenseDto.builder()
                .id(expenseId)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(expense));
        given(expenseMapper.toDto(any(Expense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.getExpense(tripId, scheduleId, expenseId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expenseId);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
        verify(expenseMapper).toDto(any(Expense.class));
    }

    @Test
    @DisplayName("존재하지 않는 지출 조회 시 예외가 발생한다")
    void getExpenseThrowsExceptionWhenExpenseNotFound() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 999L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.getExpense(tripId, scheduleId, expenseId))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
    }

    @Test
    @DisplayName("지출 목록을 커서 페이징으로 조회할 수 있다")
    void getExpenses() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        String keyword = "";
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        PersonalExpense expense1 = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(expense1, "id", 1L);

        PersonalExpense expense2 = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("30"),
                schedule
        );
        ReflectionTestUtils.setField(expense2, "id", 2L);

        List<Expense> expenses = Arrays.asList(expense1, expense2);

        PersonalExpenseDto dto1 = PersonalExpenseDto.builder()
                .id(1L)
                .build();

        PersonalExpenseDto dto2 = PersonalExpenseDto.builder()
                .id(2L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findAllByCursor(eq(scheduleId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(expenses);
        given(expenseMapper.toDto(eq(expense1))).willReturn(dto1);
        given(expenseMapper.toDto(eq(expense2))).willReturn(dto2);

        // When
        CursorPageResponseDto<ExpenseDto> result = expenseService.getExpenses(tripId, scheduleId, keyword, orderBy, direction, cursor, after, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findAllByCursor(eq(scheduleId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("개인 지출을 수정할 수 있다")
    void updatePersonalExpense() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        PersonalExpense personalExpense = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(personalExpense, "id", expenseId);

        PersonalExpenseUpdateRequestDto requestDto = PersonalExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 13, 0))
                .memo("점심 식사 수정")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))
                .totalAmount(new BigDecimal("60"))
                .build();

        PersonalExpenseDto expectedDto = PersonalExpenseDto.builder()
                .id(expenseId)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(personalExpense));
        given(expenseMapper.toDto(any(PersonalExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PersonalExpenseDto.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
        verify(expenseMapper).toDto(any(PersonalExpense.class));
    }

    @Test
    @DisplayName("공동 지출을 수정할 수 있다")
    void updateSharedExpense() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;
        Long payerId = 1L;

        User user2 = User.createUser(
                "user2",
                "password2",
                "user2@example.com",
                null,
                null,
                new HashSet<>()
        );
        ReflectionTestUtils.setField(user2, "id", 2L);

        UserProfile userProfile2 = UserProfile.createUserProfile("유저2", "https://example.com/profile2.jpg");
        ReflectionTestUtils.setField(user2, "userProfile", userProfile2);

        TripJoin tripJoin2 = TripJoin.createTripJoin(trip, user2);
        ReflectionTestUtils.setField(tripJoin2, "id", 2L);

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                CalculateType.EQUAL,
                tripJoin,
                new ArrayList<>(),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", expenseId);

        List<ExpenseParticipantRequestDto> participants = Arrays.asList(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build(),
                ExpenseParticipantRequestDto.builder()
                        .id(2L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseUpdateRequestDto requestDto = SharedExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 19, 0))
                .memo("저녁 식사 수정")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        SharedExpenseDto expectedDto = SharedExpenseDto.builder()
                .id(expenseId)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(sharedExpense));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList())).willReturn(Arrays.asList(tripJoin, tripJoin2));
        given(expenseMapper.toDto(any(SharedExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SharedExpenseDto.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
        verify(tripJoinRepository).findByUserIdAndTripId(eq(payerId), eq(tripId));
        verify(tripJoinRepository).findByTripIdAndUserIdIn(eq(tripId), anyList());
    }

    @Test
    @DisplayName("지출을 삭제할 수 있다")
    void deleteExpense() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        PersonalExpense expense = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(expense, "id", expenseId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(expense));
        willDoNothing().given(expenseRepository).delete(any(Expense.class));

        // When
        expenseService.deleteExpense(tripId, scheduleId, expenseId);

        // Then
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
        verify(expenseRepository).delete(any(Expense.class));
    }

    @Test
    @DisplayName("지출 통계를 조회할 수 있다")
    void getExpenseStatistics() {
        // Given
        Long tripId = 1L;
        ExpenseType expenseType = ExpenseType.PERSONAL;
        GroupByType groupBy = GroupByType.CATEGORY;
        LocalDate date = LocalDate.of(2024, 1, 1);

        ExpenseStatisticsDto expectedStatistics = ExpenseStatisticsDto.builder()
                .totalAmount(new BigDecimal("1000"))
                .build();

        given(expenseRepository.getExpenseStatistics(eq(tripId), eq(expenseType), eq(groupBy), eq(date)))
                .willReturn(expectedStatistics);

        // When
        ExpenseStatisticsDto result = expenseService.getExpenseStatistics(tripId, expenseType, groupBy, date);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("1000"));
        verify(expenseRepository).getExpenseStatistics(eq(tripId), eq(expenseType), eq(groupBy), eq(date));
    }

    @Test
    @DisplayName("지출 내역을 엑셀로 내보낼 수 있다")
    void exportExpensesToExcel() {
        // Given
        Long tripId = 1L;

        PersonalExpense expense1 = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(expense1, "id", 1L);

        List<Expense> expenses = List.of(expense1);

        given(expenseRepository.findAllByTripId(eq(tripId))).willReturn(expenses);

        // When
        ByteArrayResource result = expenseService.exportExpensesToExcel(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getByteArray()).isNotEmpty();
        verify(expenseRepository).findAllByTripId(eq(tripId));
    }

    @Test
    @DisplayName("지불자가 없으면 지출 생성 시 예외가 발생한다")
    void createSharedExpenseThrowsExceptionWhenPayerNotFound() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long payerId = 999L;

        List<ExpenseParticipantRequestDto> participants = List.of(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseCreateRequestDto requestDto = SharedExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.SHARED)
                .dateTime(LocalDateTime.of(2024, 1, 1, 18, 0))
                .memo("저녁 식사")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(tripJoinRepository).findByUserIdAndTripId(eq(payerId), eq(tripId));
    }

    @Test
    @DisplayName("환율 정보가 없고 요청에도 없으면 예외가 발생한다")
    void createExpenseThrowsExceptionWhenExchangeRateNotFound() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;

        PersonalExpenseCreateRequestDto requestDto = PersonalExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.PERSONAL)
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("점심 식사")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(null)
                .totalAmount(new BigDecimal("50"))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD)))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(TripCurrencyException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(tripCurrencyRepository).findByTripAndCurrencyType(eq(trip), eq(CurrencyType.USD));
    }

    @Test
    @DisplayName("중복된 참가자가 있으면 예외가 발생한다")
    void createSharedExpenseThrowsExceptionWhenDuplicateParticipants() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long payerId = 1L;

        List<ExpenseParticipantRequestDto> participants = Arrays.asList(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build(),
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseCreateRequestDto requestDto = SharedExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.SHARED)
                .dateTime(LocalDateTime.of(2024, 1, 1, 18, 0))
                .memo("저녁 식사")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(tripJoinRepository).findByUserIdAndTripId(eq(payerId), eq(tripId));
    }

    @Test
    @DisplayName("일정이 여행에 속하지 않으면 예외가 발생한다")
    void createExpenseThrowsExceptionWhenScheduleNotBelongToTrip() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;

        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/image2.jpg",
                new HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        Schedule anotherSchedule = Schedule.createSchedule(
                "다른 일정",
                LocalDateTime.of(2024, 2, 1, 9, 0),
                LocalDateTime.of(2024, 2, 1, 18, 0),
                "메모",
                1L,
                null,
                anotherTrip,
                null
        );
        ReflectionTestUtils.setField(anotherSchedule, "id", scheduleId);

        PersonalExpenseCreateRequestDto requestDto = PersonalExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.PERSONAL)
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("점심 식사")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .totalAmount(new BigDecimal("50"))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(anotherSchedule));

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(ScheduleException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
    }

    @Test
    @DisplayName("참가자를 찾을 수 없으면 예외가 발생한다")
    void createSharedExpenseThrowsExceptionWhenParticipantNotFound() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long payerId = 1L;

        List<ExpenseParticipantRequestDto> participants = Arrays.asList(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build(),
                ExpenseParticipantRequestDto.builder()
                        .id(999L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseCreateRequestDto requestDto = SharedExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.SHARED)
                .dateTime(LocalDateTime.of(2024, 1, 1, 18, 0))
                .memo("저녁 식사")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList())).willReturn(List.of(tripJoin));

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(tripJoinRepository).findByUserIdAndTripId(eq(payerId), eq(tripId));
    }

    @Test
    @DisplayName("유효하지 않은 지출 타입으로 생성 시 예외가 발생한다")
    void createExpenseThrowsExceptionWhenInvalidExpenseType() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;

        ExpenseCreateRequestDto requestDto = new ExpenseCreateRequestDto() {
            @Override
            public ExpenseType getExpenseType() {
                return null;
            }
        };

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
    }

    @Test
    @DisplayName("유효하지 않은 계산 타입으로 공동 지출 생성 시 예외가 발생한다")
    void createSharedExpenseThrowsExceptionWhenInvalidCalculateType() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long payerId = 1L;

        List<ExpenseParticipantRequestDto> participants = List.of(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseCreateRequestDto requestDto = SharedExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.SHARED)
                .dateTime(LocalDateTime.of(2024, 1, 1, 18, 0))
                .memo("저녁 식사")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1300"))
                .calculateType(null)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, scheduleId, requestDto))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(tripJoinRepository).findByUserIdAndTripId(eq(payerId), eq(tripId));
    }

    @Test
    @DisplayName("개인 지출 수정 시 환율 정보가 없으면 예외가 발생한다")
    void updatePersonalExpenseThrowsExceptionWhenExchangeRateNotFound() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        PersonalExpense personalExpense = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(personalExpense, "id", expenseId);

        PersonalExpenseUpdateRequestDto requestDto = PersonalExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 13, 0))
                .memo("점심 식사 수정")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.EUR)
                .exchangeRate(null)
                .totalAmount(new BigDecimal("60"))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(personalExpense));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.EUR)))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto))
                .isInstanceOf(TripCurrencyException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
    }

    @Test
    @DisplayName("공동 지출 수정 시 환율 정보가 없으면 예외가 발생한다")
    void updateSharedExpenseThrowsExceptionWhenExchangeRateNotFound() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;
        Long payerId = 1L;

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                CalculateType.EQUAL,
                tripJoin,
                new ArrayList<>(),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", expenseId);

        List<ExpenseParticipantRequestDto> participants = List.of(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseUpdateRequestDto requestDto = SharedExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 19, 0))
                .memo("저녁 식사 수정")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.EUR)
                .exchangeRate(null)
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(sharedExpense));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList())).willReturn(List.of(tripJoin));
        given(tripCurrencyRepository.findByTripAndCurrencyType(eq(trip), eq(CurrencyType.EUR)))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto))
                .isInstanceOf(TripCurrencyException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
    }

    @Test
    @DisplayName("유효하지 않은 지출 타입으로 수정 시 예외가 발생한다")
    void updateExpenseThrowsExceptionWhenInvalidExpenseType() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        Expense expense = new Expense() {};
        ReflectionTestUtils.setField(expense, "id", expenseId);

        ExpenseUpdateRequestDto requestDto = new PersonalExpenseUpdateRequestDto();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(expense));

        // When & Then
        assertThatThrownBy(() -> expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
    }

    @Test
    @DisplayName("유효하지 않은 계산 타입으로 공동 지출 수정 시 예외가 발생한다")
    void updateSharedExpenseThrowsExceptionWhenInvalidCalculateType() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;
        Long payerId = 1L;

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                CalculateType.EQUAL,
                tripJoin,
                new ArrayList<>(),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", expenseId);

        List<ExpenseParticipantRequestDto> participants = List.of(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseUpdateRequestDto requestDto = SharedExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 19, 0))
                .memo("저녁 식사 수정")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))
                .calculateType(null)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(sharedExpense));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));

        // When & Then
        assertThatThrownBy(() -> expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
    }

    @Test
    @DisplayName("공동 지출 수정 시 지불자를 찾을 수 없으면 예외가 발생한다")
    void updateSharedExpenseThrowsExceptionWhenPayerNotFound() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;
        Long payerId = 999L;

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                CalculateType.EQUAL,
                tripJoin,
                new ArrayList<>(),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", expenseId);

        List<ExpenseParticipantRequestDto> participants = List.of(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseUpdateRequestDto requestDto = SharedExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 19, 0))
                .memo("저녁 식사 수정")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(sharedExpense));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto))
                .isInstanceOf(ExpenseException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
        verify(tripJoinRepository).findByUserIdAndTripId(eq(payerId), eq(tripId));
    }

    @Test
    @DisplayName("지출 목록 조회 시 다음 페이지가 있으면 커서를 반환한다")
    void getExpensesReturnsNextCursor() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        String keyword = "";
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 2;

        PersonalExpense expense1 = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(expense1, "id", 1L);
        ReflectionTestUtils.setField(expense1, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0));

        PersonalExpense expense2 = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("30"),
                schedule
        );
        ReflectionTestUtils.setField(expense2, "id", 2L);
        ReflectionTestUtils.setField(expense2, "createdAt", LocalDateTime.of(2024, 1, 1, 18, 0));

        PersonalExpense expense3 = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 20, 0),
                "야식",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("20"),
                schedule
        );
        ReflectionTestUtils.setField(expense3, "id", 3L);
        ReflectionTestUtils.setField(expense3, "createdAt", LocalDateTime.of(2024, 1, 1, 20, 0));

        List<Expense> expenses = Arrays.asList(expense1, expense2, expense3);

        PersonalExpenseDto dto1 = PersonalExpenseDto.builder()
                .id(1L)
                .build();

        PersonalExpenseDto dto2 = PersonalExpenseDto.builder()
                .id(2L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findAllByCursor(eq(scheduleId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(expenses);
        given(expenseMapper.toDto(eq(expense1))).willReturn(dto1);
        given(expenseMapper.toDto(eq(expense2))).willReturn(dto2);

        // When
        CursorPageResponseDto<ExpenseDto> result = expenseService.getExpenses(tripId, scheduleId, keyword, orderBy, direction, cursor, after, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isEqualTo(2L);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findAllByCursor(eq(scheduleId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("지출 내역을 엑셀로 내보낸다 - 개인 지출만 있는 경우")
    void exportExpensesToExcelWithPersonalExpensesOnly() {
        // Given
        Long tripId = 1L;

        PersonalExpense expense1 = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );

        List<Expense> expenses = List.of(expense1);

        given(expenseRepository.findAllByTripId(eq(tripId))).willReturn(expenses);

        // When
        ByteArrayResource result = expenseService.exportExpensesToExcel(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getByteArray()).isNotEmpty();
        verify(expenseRepository).findAllByTripId(eq(tripId));
    }

    @Test
    @DisplayName("지출 내역을 엑셀로 내보낸다 - 공동 지출만 있는 경우")
    void exportExpensesToExcelWithSharedExpensesOnly() {
        // Given
        Long tripId = 1L;

        List<ExpenseParticipant> participants = new ArrayList<>();
        ExpenseParticipant participant1 = ExpenseParticipant.createExpenseParticipant(tripJoin, new BigDecimal("50"));
        participants.add(participant1);

        SharedExpense expense1 = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                CalculateType.EQUAL,
                tripJoin,
                participants,
                schedule
        );

        List<Expense> expenses = List.of(expense1);

        given(expenseRepository.findAllByTripId(eq(tripId))).willReturn(expenses);

        // When
        ByteArrayResource result = expenseService.exportExpensesToExcel(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getByteArray()).isNotEmpty();
        verify(expenseRepository).findAllByTripId(eq(tripId));
    }

    @Test
    @DisplayName("지출 내역을 엑셀로 내보낸다 - 빈 리스트")
    void exportExpensesToExcelWithEmptyList() {
        // Given
        Long tripId = 1L;
        List<Expense> expenses = List.of();

        given(expenseRepository.findAllByTripId(eq(tripId))).willReturn(expenses);

        // When
        ByteArrayResource result = expenseService.exportExpensesToExcel(tripId);

        // Then
        assertThat(result).isNotNull();
        verify(expenseRepository).findAllByTripId(eq(tripId));
    }

    @Test
    @DisplayName("정산 정보를 조회한다")
    void settleExpenses() {
        // Given
        Long tripId = 1L;

        User user2 = User.createUser(
                "user2",
                "password2",
                "user2@example.com",
                null,
                null,
                new HashSet<>()
        );
        ReflectionTestUtils.setField(user2, "id", 2L);

        UserProfile userProfile2 = UserProfile.createUserProfile("유저2", "https://example.com/profile2.jpg");
        ReflectionTestUtils.setField(user2, "userProfile", userProfile2);

        TripJoin tripJoin2 = TripJoin.createTripJoin(trip, user2);
        ReflectionTestUtils.setField(tripJoin2, "id", 2L);

        // 공동 예산 설정
        SharedBudget sharedBudget = SharedBudget.createSharedBudget(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                "공동 예산",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.ONE,
                CalculateType.EQUAL,
                new ArrayList<>(),
                trip
        );
        ReflectionTestUtils.setField(sharedBudget, "id", 1L);

        BudgetParticipant budgetParticipant1 = BudgetParticipant.createBudgetParticipant(
                tripJoin,
                new BigDecimal("500000")
        );
        BudgetParticipant budgetParticipant2 = BudgetParticipant.createBudgetParticipant(
                tripJoin2,
                new BigDecimal("500000")
        );
        sharedBudget.getBudgetParticipants().add(budgetParticipant1);
        sharedBudget.getBudgetParticipants().add(budgetParticipant2);

        // 공동 지출
        ExpenseParticipant expenseParticipant1 = ExpenseParticipant.createExpenseParticipant(
                tripJoin,
                new BigDecimal("300000")
        );
        ExpenseParticipant expenseParticipant2 = ExpenseParticipant.createExpenseParticipant(
                tripJoin2,
                new BigDecimal("300000")
        );

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.KRW,
                BigDecimal.ONE,
                CalculateType.EQUAL,
                tripJoin,
                Arrays.asList(expenseParticipant1, expenseParticipant2),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", 1L);

        given(budgetRepository.findSharedBudgetExpenseAllByTripId(eq(tripId)))
                .willReturn(List.of(sharedBudget));
        given(expenseRepository.findSharedExpenseAllByTripId(eq(tripId)))
                .willReturn(List.of(sharedExpense));

        // When
        SettleExpenseDto result = expenseService.settleExpenses(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBudget()).isNotNull();
        assertThat(result.getExpenseList()).isNotEmpty();
        verify(budgetRepository).findSharedBudgetExpenseAllByTripId(eq(tripId));
        verify(expenseRepository).findSharedExpenseAllByTripId(eq(tripId));
    }

    @Test
    @DisplayName("정산 정보 조회 시 예산과 지출이 없으면 빈 리스트를 반환한다")
    void settleExpensesWithEmptyData() {
        // Given
        Long tripId = 1L;

        given(budgetRepository.findSharedBudgetExpenseAllByTripId(eq(tripId)))
                .willReturn(List.of());
        given(expenseRepository.findSharedExpenseAllByTripId(eq(tripId)))
                .willReturn(List.of());

        // When
        SettleExpenseDto result = expenseService.settleExpenses(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBudget()).isNotNull();
        assertThat(result.getExpenseList()).hasSize(0);
        assertThat(result.getTransferList()).hasSize(0);
        verify(budgetRepository).findSharedBudgetExpenseAllByTripId(eq(tripId));
        verify(expenseRepository).findSharedExpenseAllByTripId(eq(tripId));
    }

    @Test
    @DisplayName("엑셀 내보내기 시 공동 지출에 지불자가 없는 경우도 처리한다")
    void exportExpensesToExcelWithSharedExpenseWithoutPayer() {
        // Given
        Long tripId = 1L;

        List<ExpenseParticipant> participants = new ArrayList<>();
        ExpenseParticipant participant1 = ExpenseParticipant.createExpenseParticipant(tripJoin, new BigDecimal("50"));
        participants.add(participant1);

        SharedExpense expense1 = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                CalculateType.EQUAL,
                null,  // 지불자 없음
                participants,
                schedule
        );

        List<Expense> expenses = List.of(expense1);

        given(expenseRepository.findAllByTripId(eq(tripId))).willReturn(expenses);

        // When
        ByteArrayResource result = expenseService.exportExpensesToExcel(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getByteArray()).isNotEmpty();
        verify(expenseRepository).findAllByTripId(eq(tripId));
    }

    @Test
    @DisplayName("개인 지출 생성 시 환율이 제공되면 제공된 환율을 사용한다")
    void createPersonalExpenseWithProvidedExchangeRate() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;

        PersonalExpenseCreateRequestDto requestDto = PersonalExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.PERSONAL)
                .dateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .memo("점심 식사")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))  // 환율 제공
                .totalAmount(new BigDecimal("50"))
                .build();

        PersonalExpenseDto expectedDto = PersonalExpenseDto.builder()
                .id(1L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseMapper.toDto(any(PersonalExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.createExpense(tripId, scheduleId, requestDto);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).save(any(PersonalExpense.class));
        verify(tripCurrencyRepository, never()).findByTripAndCurrencyType(any(), any());
    }

    @Test
    @DisplayName("공동 지출 생성 시 환율이 제공되면 제공된 환율을 사용한다")
    void createSharedExpenseWithProvidedExchangeRate() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long payerId = 1L;

        List<ExpenseParticipantRequestDto> participants = List.of(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseCreateRequestDto requestDto = SharedExpenseCreateRequestDto.builder()
                .expenseType(ExpenseType.SHARED)
                .dateTime(LocalDateTime.of(2024, 1, 1, 18, 0))
                .memo("저녁 식사")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))  // 환율 제공
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        SharedExpenseDto expectedDto = SharedExpenseDto.builder()
                .id(1L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList())).willReturn(List.of(tripJoin));
        given(expenseMapper.toDto(any(SharedExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.createExpense(tripId, scheduleId, requestDto);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).save(any(SharedExpense.class));
        verify(tripCurrencyRepository, never()).findByTripAndCurrencyType(any(), any());
    }

    @Test
    @DisplayName("개인 지출 수정 시 환율이 제공되면 제공된 환율을 사용한다")
    void updatePersonalExpenseWithProvidedExchangeRate() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        PersonalExpense personalExpense = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(personalExpense, "id", expenseId);

        PersonalExpenseUpdateRequestDto requestDto = PersonalExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 13, 0))
                .memo("점심 식사 수정")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))  // 환율 제공
                .totalAmount(new BigDecimal("60"))
                .build();

        PersonalExpenseDto expectedDto = PersonalExpenseDto.builder()
                .id(expenseId)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(personalExpense));
        given(expenseMapper.toDto(any(PersonalExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
        verify(tripCurrencyRepository, never()).findByTripAndCurrencyType(any(), any());
    }

    @Test
    @DisplayName("공동 지출 수정 시 환율이 제공되면 제공된 환율을 사용한다")
    void updateSharedExpenseWithProvidedExchangeRate() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;
        Long payerId = 1L;

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                CalculateType.EQUAL,
                tripJoin,
                new ArrayList<>(),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", expenseId);

        List<ExpenseParticipantRequestDto> participants = List.of(
                ExpenseParticipantRequestDto.builder()
                        .id(1L)
                        .amount(new BigDecimal("100"))
                        .build()
        );

        SharedExpenseUpdateRequestDto requestDto = SharedExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 19, 0))
                .memo("저녁 식사 수정")
                .paymentMethod(PaymentMethod.CARD)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))  // 환율 제공
                .calculateType(CalculateType.EQUAL)
                .payerId(payerId)
                .expenseParticipants(participants)
                .build();

        SharedExpenseDto expectedDto = SharedExpenseDto.builder()
                .id(expenseId)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findById(eq(expenseId))).willReturn(Optional.of(sharedExpense));
        given(tripJoinRepository.findByUserIdAndTripId(eq(payerId), eq(tripId))).willReturn(Optional.of(tripJoin));
        given(tripJoinRepository.findByTripIdAndUserIdIn(eq(tripId), anyList())).willReturn(List.of(tripJoin));
        given(expenseMapper.toDto(any(SharedExpense.class))).willReturn(expectedDto);

        // When
        ExpenseDto result = expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findById(eq(expenseId));
        verify(tripCurrencyRepository, never()).findByTripAndCurrencyType(any(), any());
    }

    @Test
    @DisplayName("지출 목록 조회 시 다음 페이지가 없으면 커서를 반환하지 않는다")
    void getExpensesWithoutNextPage() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        String keyword = "";
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        PersonalExpense expense1 = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );
        ReflectionTestUtils.setField(expense1, "id", 1L);

        List<Expense> expenses = List.of(expense1);

        PersonalExpenseDto dto1 = PersonalExpenseDto.builder()
                .id(1L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(expenseRepository.findAllByCursor(eq(scheduleId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(expenses);
        given(expenseMapper.toDto(eq(expense1))).willReturn(dto1);

        // When
        CursorPageResponseDto<ExpenseDto> result = expenseService.getExpenses(tripId, scheduleId, keyword, orderBy, direction, cursor, after, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
        verify(expenseRepository).findAllByCursor(eq(scheduleId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("지출 삭제 시 일정이 여행에 속하지 않으면 예외가 발생한다")
    void deleteExpenseThrowsExceptionWhenScheduleNotBelongToTrip() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/image2.jpg",
                new HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        Schedule anotherSchedule = Schedule.createSchedule(
                "다른 일정",
                LocalDateTime.of(2024, 2, 1, 9, 0),
                LocalDateTime.of(2024, 2, 1, 18, 0),
                "메모",
                1L,
                null,
                anotherTrip,
                null
        );
        ReflectionTestUtils.setField(anotherSchedule, "id", scheduleId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(anotherSchedule));

        // When & Then
        assertThatThrownBy(() -> expenseService.deleteExpense(tripId, scheduleId, expenseId))
                .isInstanceOf(ScheduleException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
    }

    @Test
    @DisplayName("지출 조회 시 일정이 여행에 속하지 않으면 예외가 발생한다")
    void getExpenseThrowsExceptionWhenScheduleNotBelongToTrip() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/image2.jpg",
                new HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        Schedule anotherSchedule = Schedule.createSchedule(
                "다른 일정",
                LocalDateTime.of(2024, 2, 1, 9, 0),
                LocalDateTime.of(2024, 2, 1, 18, 0),
                "메모",
                1L,
                null,
                anotherTrip,
                null
        );
        ReflectionTestUtils.setField(anotherSchedule, "id", scheduleId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(anotherSchedule));

        // When & Then
        assertThatThrownBy(() -> expenseService.getExpense(tripId, scheduleId, expenseId))
                .isInstanceOf(ScheduleException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
    }

    @Test
    @DisplayName("지출 목록 조회 시 일정이 여행에 속하지 않으면 예외가 발생한다")
    void getExpensesThrowsExceptionWhenScheduleNotBelongToTrip() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;

        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/image2.jpg",
                new HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        Schedule anotherSchedule = Schedule.createSchedule(
                "다른 일정",
                LocalDateTime.of(2024, 2, 1, 9, 0),
                LocalDateTime.of(2024, 2, 1, 18, 0),
                "메모",
                1L,
                null,
                anotherTrip,
                null
        );
        ReflectionTestUtils.setField(anotherSchedule, "id", scheduleId);

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(anotherSchedule));

        // When & Then
        assertThatThrownBy(() -> expenseService.getExpenses(tripId, scheduleId, "", "createdAt", "DESC", null, null, 10))
                .isInstanceOf(ScheduleException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
    }

    @Test
    @DisplayName("지출 수정 시 일정이 여행에 속하지 않으면 예외가 발생한다")
    void updateExpenseThrowsExceptionWhenScheduleNotBelongToTrip() {
        // Given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Long expenseId = 1L;

        Trip anotherTrip = Trip.createTrip(
                "다른 여행",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 10),
                "https://example.com/image2.jpg",
                new HashSet<>()
        );
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        Schedule anotherSchedule = Schedule.createSchedule(
                "다른 일정",
                LocalDateTime.of(2024, 2, 1, 9, 0),
                LocalDateTime.of(2024, 2, 1, 18, 0),
                "메모",
                1L,
                null,
                anotherTrip,
                null
        );
        ReflectionTestUtils.setField(anotherSchedule, "id", scheduleId);

        PersonalExpenseUpdateRequestDto requestDto = PersonalExpenseUpdateRequestDto.builder()
                .datetime(LocalDateTime.of(2024, 1, 1, 13, 0))
                .memo("점심 식사 수정")
                .paymentMethod(PaymentMethod.CASH)
                .category(travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD)
                .currencyType(CurrencyType.USD)
                .exchangeRate(new BigDecimal("1350"))
                .totalAmount(new BigDecimal("60"))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(anotherSchedule));

        // When & Then
        assertThatThrownBy(() -> expenseService.updateExpense(tripId, scheduleId, expenseId, requestDto))
                .isInstanceOf(ScheduleException.class);
        verify(tripRepository).findById(eq(tripId));
        verify(scheduleRepository).findById(eq(scheduleId));
    }

    @Test
    @DisplayName("엑셀 내보내기 시 개인 지출과 공동 지출이 섞여 있어도 처리한다")
    void exportExpensesToExcelWithMixedExpenses() {
        // Given
        Long tripId = 1L;

        PersonalExpense personalExpense = PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "점심 식사",
                PaymentMethod.CARD,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                new BigDecimal("50"),
                schedule
        );

        List<ExpenseParticipant> participants = new ArrayList<>();
        ExpenseParticipant participant1 = ExpenseParticipant.createExpenseParticipant(tripJoin, new BigDecimal("50"));
        participants.add(participant1);

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.USD,
                new BigDecimal("1300"),
                CalculateType.EQUAL,
                tripJoin,
                participants,
                schedule
        );

        List<Expense> expenses = Arrays.asList(personalExpense, sharedExpense);

        given(expenseRepository.findAllByTripId(eq(tripId))).willReturn(expenses);

        // When
        ByteArrayResource result = expenseService.exportExpensesToExcel(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getByteArray()).isNotEmpty();
        verify(expenseRepository).findAllByTripId(eq(tripId));
    }

    @Test
    @DisplayName("정산 정보 조회 시 지불자가 null인 공동 지출도 처리한다")
    void settleExpensesWithNullPayer() {
        // Given
        Long tripId = 1L;

        User user2 = User.createUser(
                "user2",
                "password2",
                "user2@example.com",
                null,
                null,
                new HashSet<>()
        );
        ReflectionTestUtils.setField(user2, "id", 2L);

        UserProfile userProfile2 = UserProfile.createUserProfile("유저2", "https://example.com/profile2.jpg");
        ReflectionTestUtils.setField(user2, "userProfile", userProfile2);

        TripJoin tripJoin2 = TripJoin.createTripJoin(trip, user2);
        ReflectionTestUtils.setField(tripJoin2, "id", 2L);

        SharedBudget sharedBudget = SharedBudget.createSharedBudget(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                "공동 예산",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.ONE,
                CalculateType.EQUAL,
                new ArrayList<>(),
                trip
        );
        ReflectionTestUtils.setField(sharedBudget, "id", 1L);

        BudgetParticipant budgetParticipant1 = BudgetParticipant.createBudgetParticipant(
                tripJoin,
                new BigDecimal("500000")
        );
        BudgetParticipant budgetParticipant2 = BudgetParticipant.createBudgetParticipant(
                tripJoin2,
                new BigDecimal("500000")
        );
        sharedBudget.getBudgetParticipants().add(budgetParticipant1);
        sharedBudget.getBudgetParticipants().add(budgetParticipant2);

        ExpenseParticipant expenseParticipant1 = ExpenseParticipant.createExpenseParticipant(
                tripJoin,
                new BigDecimal("300000")
        );
        ExpenseParticipant expenseParticipant2 = ExpenseParticipant.createExpenseParticipant(
                tripJoin2,
                new BigDecimal("300000")
        );

        SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 1, 18, 0),
                "저녁 식사",
                PaymentMethod.CASH,
                travel.mytravelplan.domain.expense.enums.ExpenseCategory.FOOD,
                CurrencyType.KRW,
                BigDecimal.ONE,
                CalculateType.EQUAL,
                null,  // 지불자가 null
                Arrays.asList(expenseParticipant1, expenseParticipant2),
                schedule
        );
        ReflectionTestUtils.setField(sharedExpense, "id", 1L);

        given(budgetRepository.findSharedBudgetExpenseAllByTripId(eq(tripId)))
                .willReturn(List.of(sharedBudget));
        given(expenseRepository.findSharedExpenseAllByTripId(eq(tripId)))
                .willReturn(List.of(sharedExpense));

        // When
        SettleExpenseDto result = expenseService.settleExpenses(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBudget()).isNotNull();
        verify(budgetRepository).findSharedBudgetExpenseAllByTripId(eq(tripId));
        verify(expenseRepository).findSharedExpenseAllByTripId(eq(tripId));
    }
}



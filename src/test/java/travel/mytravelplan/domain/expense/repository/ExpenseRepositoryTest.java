package travel.mytravelplan.domain.expense.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.entity.Expense;
import travel.mytravelplan.domain.expense.entity.ExpenseParticipant;
import travel.mytravelplan.domain.expense.entity.PersonalExpense;
import travel.mytravelplan.domain.expense.entity.SharedExpense;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.schedule.repository.ScheduleRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지출 레포지토리 테스트")
class ExpenseRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripJoinRepository tripJoinRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    @DisplayName("개인 지출을 저장한다")
    void savePersonalExpense() {
        // given
        Trip trip = createAndSaveTrip("제주도 여행", LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 5));
        Schedule schedule = createAndSaveSchedule("호텔 체크인", LocalDateTime.of(2024, 7, 1, 14, 0), LocalDateTime.of(2024, 7, 1, 15, 0), trip);
        PersonalExpense personalExpense = createPersonalExpense(
                LocalDateTime.of(2024, 7, 1, 10, 0),
                "호텔 비용",
                PaymentMethod.CARD,
                ExpenseCategory.ACCOMMODATION,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(150000),
                schedule
        );

        // when
        Expense savedExpense = expenseRepository.save(personalExpense);
        em.flush();
        em.clear();

        // then
        Expense foundExpense = expenseRepository.findById(savedExpense.getId()).orElse(null);
        assertThat(foundExpense).isNotNull();
        assertThat(foundExpense).isInstanceOf(PersonalExpense.class);
        assertThat(foundExpense.getMemo()).isEqualTo("호텔 비용");
        assertThat(foundExpense.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(foundExpense.getCategory()).isEqualTo(ExpenseCategory.ACCOMMODATION);
        assertThat(((PersonalExpense) foundExpense).getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150000));
    }

    @Test
    @DisplayName("공유 지출을 저장한다")
    void saveSharedExpense() {
        // given
        Trip trip = createAndSaveTrip("일본 여행", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 10));
        Schedule schedule = createAndSaveSchedule("렌터카 픽업", LocalDateTime.of(2024, 8, 1, 10, 0), LocalDateTime.of(2024, 8, 1, 11, 0), trip);
        User user1 = createAndSaveUser("user1", "user1@email.com");
        User user2 = createAndSaveUser("user2", "user2@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        ExpenseParticipant participant1 = createExpenseParticipant(tripJoin1, BigDecimal.valueOf(50000));
        ExpenseParticipant participant2 = createExpenseParticipant(tripJoin2, BigDecimal.valueOf(50000));

        SharedExpense sharedExpense = createSharedExpense(
                LocalDateTime.of(2024, 8, 1, 12, 0),
                "렌터카 비용",
                PaymentMethod.CASH,
                ExpenseCategory.TRANSPORTATION,
                CurrencyType.JPY,
                BigDecimal.valueOf(9.5),
                CalculateType.EQUAL,
                tripJoin1,
                List.of(participant1, participant2),
                schedule
        );

        // when
        Expense savedExpense = expenseRepository.save(sharedExpense);
        em.flush();
        em.clear();

        // then
        Expense foundExpense = expenseRepository.findById(savedExpense.getId()).orElse(null);
        assertThat(foundExpense).isNotNull();
        assertThat(foundExpense).isInstanceOf(SharedExpense.class);
        assertThat(foundExpense.getMemo()).isEqualTo("렌터카 비용");
        assertThat(((SharedExpense) foundExpense).getCalculateType()).isEqualTo(CalculateType.EQUAL);
        assertThat(((SharedExpense) foundExpense).getExpenseParticipants()).hasSize(2);
    }

    @Test
    @DisplayName("지출을 ID로 조회한다")
    void findExpenseById() {
        // given
        Trip trip = createAndSaveTrip("부산 여행", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 3));
        Schedule schedule = createAndSaveSchedule("버스 이동", LocalDateTime.of(2024, 9, 1, 9, 0), LocalDateTime.of(2024, 9, 1, 12, 0), trip);
        PersonalExpense personalExpense = createPersonalExpense(
                LocalDateTime.of(2024, 9, 1, 14, 0),
                "교통비",
                PaymentMethod.CARD,
                ExpenseCategory.TRANSPORTATION,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(50000),
                schedule
        );
        Expense savedExpense = expenseRepository.save(personalExpense);
        em.flush();
        em.clear();

        // when
        Expense foundExpense = expenseRepository.findById(savedExpense.getId()).orElse(null);

        // then
        assertThat(foundExpense).isNotNull();
        assertThat(foundExpense.getId()).isEqualTo(savedExpense.getId());
        assertThat(foundExpense.getMemo()).isEqualTo("교통비");
    }

    @Test
    @DisplayName("개인 지출을 수정한다")
    void updatePersonalExpense() {
        // given
        Trip trip = createAndSaveTrip("강릉 여행", LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 3));
        Schedule schedule = createAndSaveSchedule("점심 식사", LocalDateTime.of(2024, 10, 1, 12, 0), LocalDateTime.of(2024, 10, 1, 13, 0), trip);
        PersonalExpense personalExpense = createPersonalExpense(
                LocalDateTime.of(2024, 10, 1, 10, 0),
                "식비",
                PaymentMethod.CARD,
                ExpenseCategory.FOOD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                schedule
        );
        Expense savedExpense = expenseRepository.save(personalExpense);
        em.flush();
        em.clear();

        // when
        PersonalExpense foundExpense = (PersonalExpense) expenseRepository.findById(savedExpense.getId()).orElseThrow();
        foundExpense.update(
                LocalDateTime.of(2024, 10, 1, 12, 0),
                "점심 식비",
                PaymentMethod.CASH,
                ExpenseCategory.FOOD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(45000)
        );
        em.flush();
        em.clear();

        // then
        PersonalExpense updatedExpense = (PersonalExpense) expenseRepository.findById(savedExpense.getId()).orElse(null);
        assertThat(updatedExpense).isNotNull();
        assertThat(updatedExpense.getMemo()).isEqualTo("점심 식비");
        assertThat(updatedExpense.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(updatedExpense.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(45000));
    }

    @Test
    @DisplayName("공유 지출을 수정한다")
    void updateSharedExpense() {
        // given
        Trip trip = createAndSaveTrip("유럽 여행", LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 10));
        Schedule schedule = createAndSaveSchedule("호텔 체크인", LocalDateTime.of(2024, 11, 1, 15, 0), LocalDateTime.of(2024, 11, 1, 16, 0), trip);
        User user1 = createAndSaveUser("user3", "user3@email.com");
        User user2 = createAndSaveUser("user4", "user4@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        ExpenseParticipant participant1 = createExpenseParticipant(tripJoin1, BigDecimal.valueOf(100000));
        ExpenseParticipant participant2 = createExpenseParticipant(tripJoin2, BigDecimal.valueOf(100000));

        SharedExpense sharedExpense = createSharedExpense(
                LocalDateTime.of(2024, 11, 1, 10, 0),
                "숙박비",
                PaymentMethod.CARD,
                ExpenseCategory.ACCOMMODATION,
                CurrencyType.EUR,
                BigDecimal.valueOf(1450),
                CalculateType.EQUAL,
                tripJoin1,
                List.of(participant1, participant2),
                schedule
        );
        Expense savedExpense = expenseRepository.save(sharedExpense);
        em.flush();
        em.clear();

        // when
        SharedExpense foundExpense = (SharedExpense) expenseRepository.findById(savedExpense.getId()).orElseThrow();
        ExpenseParticipant newParticipant1 = createExpenseParticipant(tripJoin1, BigDecimal.valueOf(150000));
        ExpenseParticipant newParticipant2 = createExpenseParticipant(tripJoin2, BigDecimal.valueOf(150000));
        foundExpense.update(
                LocalDateTime.of(2024, 11, 1, 15, 0),
                "호텔 숙박비",
                PaymentMethod.CASH,
                ExpenseCategory.ACCOMMODATION,
                CurrencyType.EUR,
                BigDecimal.valueOf(1500),
                CalculateType.EACH,
                tripJoin2,
                List.of(newParticipant1, newParticipant2)
        );
        em.flush();
        em.clear();

        // then
        SharedExpense updatedExpense = (SharedExpense) expenseRepository.findById(savedExpense.getId()).orElse(null);
        assertThat(updatedExpense).isNotNull();
        assertThat(updatedExpense.getMemo()).isEqualTo("호텔 숙박비");
        assertThat(updatedExpense.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(updatedExpense.getCalculateType()).isEqualTo(CalculateType.EACH);
        assertThat(updatedExpense.getPayer().getId()).isEqualTo(tripJoin2.getId());
        assertThat(updatedExpense.getExpenseParticipants()).hasSize(2);
    }

    @Test
    @DisplayName("지출을 삭제한다")
    void deleteExpense() {
        // given
        Trip trip = createAndSaveTrip("경주 여행", LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 3));
        Schedule schedule = createAndSaveSchedule("박물관 관람", LocalDateTime.of(2024, 12, 1, 10, 0), LocalDateTime.of(2024, 12, 1, 12, 0), trip);
        PersonalExpense personalExpense = createPersonalExpense(
                LocalDateTime.of(2024, 12, 1, 10, 30),
                "입장료",
                PaymentMethod.CARD,
                ExpenseCategory.ENTERTAINMENT,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(5000),
                schedule
        );
        Expense savedExpense = expenseRepository.save(personalExpense);
        em.flush();
        em.clear();

        // when
        expenseRepository.deleteById(savedExpense.getId());
        em.flush();
        em.clear();

        // then
        Expense deletedExpense = expenseRepository.findById(savedExpense.getId()).orElse(null);
        assertThat(deletedExpense).isNull();
    }

    @Test
    @DisplayName("여행 ID로 공유 지출 목록을 조회한다")
    void findSharedExpenseAllByTripId() {
        // given
        Trip trip = createAndSaveTrip("홍콩 여행", LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 5));
        Schedule schedule1 = createAndSaveSchedule("디즈니랜드", LocalDateTime.of(2024, 6, 2, 9, 0), LocalDateTime.of(2024, 6, 2, 18, 0), trip);
        Schedule schedule2 = createAndSaveSchedule("호텔", LocalDateTime.of(2024, 6, 2, 20, 0), LocalDateTime.of(2024, 6, 3, 10, 0), trip);

        User user1 = createAndSaveUser("hong1", "hong1@email.com");
        User user2 = createAndSaveUser("hong2", "hong2@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        ExpenseParticipant participant1 = createExpenseParticipant(tripJoin1, BigDecimal.valueOf(80000));
        ExpenseParticipant participant2 = createExpenseParticipant(tripJoin2, BigDecimal.valueOf(80000));

        SharedExpense sharedExpense1 = createSharedExpense(
                LocalDateTime.of(2024, 6, 2, 9, 0),
                "디즈니랜드 입장료",
                PaymentMethod.CARD,
                ExpenseCategory.ENTERTAINMENT,
                CurrencyType.HKD,
                BigDecimal.valueOf(170),
                CalculateType.EQUAL,
                tripJoin1,
                List.of(participant1, participant2),
                schedule1
        );

        ExpenseParticipant participant3 = createExpenseParticipant(tripJoin1, BigDecimal.valueOf(100000));
        ExpenseParticipant participant4 = createExpenseParticipant(tripJoin2, BigDecimal.valueOf(100000));

        SharedExpense sharedExpense2 = createSharedExpense(
                LocalDateTime.of(2024, 6, 2, 20, 0),
                "호텔 비용",
                PaymentMethod.CARD,
                ExpenseCategory.ACCOMMODATION,
                CurrencyType.HKD,
                BigDecimal.valueOf(170),
                CalculateType.EQUAL,
                tripJoin1,
                List.of(participant3, participant4),
                schedule2
        );

        expenseRepository.save(sharedExpense1);
        expenseRepository.save(sharedExpense2);
        em.flush();
        em.clear();

        // when
        List<SharedExpense> sharedExpenses = expenseRepository.findSharedExpenseAllByTripId(trip.getId());

        // then
        assertThat(sharedExpenses).hasSize(2);
        assertThat(sharedExpenses).extracting("memo")
                .containsExactlyInAnyOrder("디즈니랜드 입장료", "호텔 비용");
    }

    @Test
    @DisplayName("여행 ID로 모든 지출 목록을 조회한다")
    void findAllByTripId() {
        // given
        Trip trip = createAndSaveTrip("방콕 여행", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 7));
        Schedule schedule1 = createAndSaveSchedule("사원 방문", LocalDateTime.of(2024, 5, 2, 10, 0), LocalDateTime.of(2024, 5, 2, 12, 0), trip);
        Schedule schedule2 = createAndSaveSchedule("마사지 샵", LocalDateTime.of(2024, 5, 2, 15, 0), LocalDateTime.of(2024, 5, 2, 17, 0), trip);
        Schedule schedule3 = createAndSaveSchedule("쇼핑", LocalDateTime.of(2024, 5, 3, 14, 0), LocalDateTime.of(2024, 5, 3, 18, 0), trip);

        User user1 = createAndSaveUser("bangkok1", "bangkok1@email.com");
        User user2 = createAndSaveUser("bangkok2", "bangkok2@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        // 개인 지출
        PersonalExpense personalExpense = createPersonalExpense(
                LocalDateTime.of(2024, 5, 2, 10, 30),
                "사원 입장료",
                PaymentMethod.CASH,
                ExpenseCategory.ENTERTAINMENT,
                CurrencyType.THB,
                BigDecimal.valueOf(37),
                BigDecimal.valueOf(200),
                schedule1
        );

        // 공유 지출
        ExpenseParticipant participant1 = createExpenseParticipant(tripJoin1, BigDecimal.valueOf(30000));
        ExpenseParticipant participant2 = createExpenseParticipant(tripJoin2, BigDecimal.valueOf(30000));

        SharedExpense sharedExpense = createSharedExpense(
                LocalDateTime.of(2024, 5, 2, 15, 0),
                "마사지 비용",
                PaymentMethod.CASH,
                ExpenseCategory.ENTERTAINMENT,
                CurrencyType.THB,
                BigDecimal.valueOf(37),
                CalculateType.EQUAL,
                tripJoin1,
                List.of(participant1, participant2),
                schedule2
        );

        // 또 다른 개인 지출
        PersonalExpense personalExpense2 = createPersonalExpense(
                LocalDateTime.of(2024, 5, 3, 14, 30),
                "쇼핑",
                PaymentMethod.CARD,
                ExpenseCategory.SHOPPING,
                CurrencyType.THB,
                BigDecimal.valueOf(37),
                BigDecimal.valueOf(5000),
                schedule3
        );

        expenseRepository.save(personalExpense);
        expenseRepository.save(sharedExpense);
        expenseRepository.save(personalExpense2);
        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByTripId(trip.getId());

        // then
        assertThat(expenses).hasSize(3);
        assertThat(expenses).extracting("memo")
                .containsExactlyInAnyOrder("사원 입장료", "마사지 비용", "쇼핑");
    }

    @Test
    @DisplayName("여행 ID로 조회 시 다른 여행의 지출은 조회되지 않는다")
    void findAllByTripId_notIncludeOtherTripExpenses() {
        // given
        Trip trip1 = createAndSaveTrip("파리 여행", LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 7));
        Trip trip2 = createAndSaveTrip("로마 여행", LocalDate.of(2024, 4, 8), LocalDate.of(2024, 4, 14));

        Schedule schedule1 = createAndSaveSchedule("에펠탑", LocalDateTime.of(2024, 4, 2, 10, 0), LocalDateTime.of(2024, 4, 2, 12, 0), trip1);
        Schedule schedule2 = createAndSaveSchedule("콜로세움", LocalDateTime.of(2024, 4, 9, 10, 0), LocalDateTime.of(2024, 4, 9, 12, 0), trip2);

        PersonalExpense expense1 = createPersonalExpense(
                LocalDateTime.of(2024, 4, 2, 10, 30),
                "에펠탑 입장료",
                PaymentMethod.CARD,
                ExpenseCategory.ENTERTAINMENT,
                CurrencyType.EUR,
                BigDecimal.valueOf(1450),
                BigDecimal.valueOf(30),
                schedule1
        );

        PersonalExpense expense2 = createPersonalExpense(
                LocalDateTime.of(2024, 4, 9, 10, 30),
                "콜로세움 입장료",
                PaymentMethod.CARD,
                ExpenseCategory.ENTERTAINMENT,
                CurrencyType.EUR,
                BigDecimal.valueOf(1450),
                BigDecimal.valueOf(16),
                schedule2
        );

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        em.flush();
        em.clear();

        // when
        List<Expense> trip1Expenses = expenseRepository.findAllByTripId(trip1.getId());
        List<Expense> trip2Expenses = expenseRepository.findAllByTripId(trip2.getId());

        // then
        assertThat(trip1Expenses).hasSize(1);
        assertThat(trip1Expenses.getFirst().getMemo()).isEqualTo("에펠탑 입장료");

        assertThat(trip2Expenses).hasSize(1);
        assertThat(trip2Expenses.getFirst().getMemo()).isEqualTo("콜로세움 입장료");
    }

    // TestFixture 메서드들
    private Trip createTrip(String title, LocalDate startDate, LocalDate endDate) {
        return Trip.createTrip(
                title,
                startDate,
                endDate,
                "https://example.com/image.jpg",
                Set.of(Country.KR)
        );
    }

    private Trip createAndSaveTrip(String title, LocalDate startDate, LocalDate endDate) {
        Trip trip = createTrip(title, startDate, endDate);
        return tripRepository.save(trip);
    }

    private User createUser(String username, String email) {
        return User.createUser(
                username,
                "password123",
                email,
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );
    }

    private User createAndSaveUser(String username, String email) {
        User user = createUser(username, email);
        return userRepository.save(user);
    }

    private TripJoin createTripJoin(Trip trip, User user) {
        return TripJoin.createTripJoin(trip, user);
    }

    private TripJoin createAndSaveTripJoin(Trip trip, User user) {
        TripJoin tripJoin = createTripJoin(trip, user);
        return tripJoinRepository.save(tripJoin);
    }

    private Schedule createSchedule(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, Trip trip) {
        return Schedule.createSchedule(
                title,
                startDateTime,
                endDateTime,
                "메모",
                1L,
                null,
                trip,
                BigDecimal.valueOf(5)
        );
    }

    private Schedule createAndSaveSchedule(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, Trip trip) {
        Schedule schedule = createSchedule(title, startDateTime, endDateTime, trip);
        return scheduleRepository.save(schedule);
    }

    private PersonalExpense createPersonalExpense(
            LocalDateTime dateTime,
            String memo,
            PaymentMethod paymentMethod,
            ExpenseCategory category,
            CurrencyType currencyType,
            BigDecimal exchangeRate,
            BigDecimal totalAmount,
            Schedule schedule
    ) {
        return PersonalExpense.createPersonalExpense(
                dateTime,
                memo,
                paymentMethod,
                category,
                currencyType,
                exchangeRate,
                totalAmount,
                schedule
        );
    }

    private SharedExpense createSharedExpense(
            LocalDateTime dateTime,
            String memo,
            PaymentMethod paymentMethod,
            ExpenseCategory category,
            CurrencyType currencyType,
            BigDecimal exchangeRate,
            CalculateType calculateType,
            TripJoin payer,
            List<ExpenseParticipant> participants,
            Schedule schedule
    ) {
        return SharedExpense.createSharedExpense(
                dateTime,
                memo,
                paymentMethod,
                category,
                currencyType,
                exchangeRate,
                calculateType,
                payer,
                participants,
                schedule
        );
    }

    private ExpenseParticipant createExpenseParticipant(TripJoin tripJoin, BigDecimal amount) {
        return ExpenseParticipant.createExpenseParticipant(tripJoin, amount);
    }
}
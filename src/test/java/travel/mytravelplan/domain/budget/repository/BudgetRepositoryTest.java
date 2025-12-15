package travel.mytravelplan.domain.budget.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.budget.entity.Budget;
import travel.mytravelplan.domain.budget.entity.BudgetParticipant;
import travel.mytravelplan.domain.budget.entity.PersonalBudget;
import travel.mytravelplan.domain.budget.entity.SharedBudget;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
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

@DisplayName("예산 레포지토리 테스트")
class BudgetRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripJoinRepository tripJoinRepository;

    @Test
    @DisplayName("개인 예산을 저장한다")
    void savePersonalBudget() {
        // given
        Trip trip = createAndSaveTrip("제주도 여행", LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 5));
        PersonalBudget personalBudget = createPersonalBudget(
                LocalDateTime.of(2024, 7, 1, 10, 0),
                "호텔 비용",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(150000),
                trip
        );

        // when
        Budget savedBudget = budgetRepository.save(personalBudget);
        em.flush();
        em.clear();

        // then
        Budget foundBudget = budgetRepository.findById(savedBudget.getId()).orElse(null);
        assertThat(foundBudget).isNotNull();
        assertThat(foundBudget).isInstanceOf(PersonalBudget.class);
        assertThat(foundBudget.getMemo()).isEqualTo("호텔 비용");
        assertThat(foundBudget.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(((PersonalBudget) foundBudget).getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150000));
    }

    @Test
    @DisplayName("공유 예산을 저장한다")
    void saveSharedBudget() {
        // given
        Trip trip = createAndSaveTrip("일본 여행", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 10));
        User user1 = createAndSaveUser("user1", "user1@email.com");
        User user2 = createAndSaveUser("user2", "user2@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        BudgetParticipant participant1 = createBudgetParticipant(tripJoin1, BigDecimal.valueOf(50000));
        BudgetParticipant participant2 = createBudgetParticipant(tripJoin2, BigDecimal.valueOf(50000));

        SharedBudget sharedBudget = createSharedBudget(
                LocalDateTime.of(2024, 8, 1, 12, 0),
                "렌터카 비용",
                PaymentMethod.CASH,
                CurrencyType.JPY,
                BigDecimal.valueOf(9.5),
                CalculateType.EQUAL,
                List.of(participant1, participant2),
                trip
        );

        // when
        Budget savedBudget = budgetRepository.save(sharedBudget);
        em.flush();
        em.clear();

        // then
        Budget foundBudget = budgetRepository.findById(savedBudget.getId()).orElse(null);
        assertThat(foundBudget).isNotNull();
        assertThat(foundBudget).isInstanceOf(SharedBudget.class);
        assertThat(foundBudget.getMemo()).isEqualTo("렌터카 비용");
        assertThat(((SharedBudget) foundBudget).getCalculateType()).isEqualTo(CalculateType.EQUAL);
        assertThat(((SharedBudget) foundBudget).getBudgetParticipants()).hasSize(2);
    }

    @Test
    @DisplayName("예산을 ID로 조회한다")
    void findBudgetById() {
        // given
        Trip trip = createAndSaveTrip("부산 여행", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 3));
        PersonalBudget personalBudget = createPersonalBudget(
                LocalDateTime.of(2024, 9, 1, 14, 0),
                "교통비",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(50000),
                trip
        );
        Budget savedBudget = budgetRepository.save(personalBudget);
        em.flush();
        em.clear();

        // when
        Budget foundBudget = budgetRepository.findById(savedBudget.getId()).orElse(null);

        // then
        assertThat(foundBudget).isNotNull();
        assertThat(foundBudget.getId()).isEqualTo(savedBudget.getId());
        assertThat(foundBudget.getMemo()).isEqualTo("교통비");
    }

    @Test
    @DisplayName("개인 예산을 수정한다")
    void updatePersonalBudget() {
        // given
        Trip trip = createAndSaveTrip("강릉 여행", LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 3));
        PersonalBudget personalBudget = createPersonalBudget(
                LocalDateTime.of(2024, 10, 1, 10, 0),
                "식비",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );
        Budget savedBudget = budgetRepository.save(personalBudget);
        em.flush();
        em.clear();

        // when
        PersonalBudget foundBudget = (PersonalBudget) budgetRepository.findById(savedBudget.getId()).orElseThrow();
        foundBudget.update(
                LocalDateTime.of(2024, 10, 1, 12, 0),
                "점심 식비",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(45000)
        );
        em.flush();
        em.clear();

        // then
        PersonalBudget updatedBudget = (PersonalBudget) budgetRepository.findById(savedBudget.getId()).orElse(null);
        assertThat(updatedBudget).isNotNull();
        assertThat(updatedBudget.getMemo()).isEqualTo("점심 식비");
        assertThat(updatedBudget.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(updatedBudget.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(45000));
    }

    @Test
    @DisplayName("공유 예산을 수정한다")
    void updateSharedBudget() {
        // given
        Trip trip = createAndSaveTrip("유럽 여행", LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 10));
        User user1 = createAndSaveUser("user3", "user3@email.com");
        User user2 = createAndSaveUser("user4", "user4@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        BudgetParticipant participant1 = createBudgetParticipant(tripJoin1, BigDecimal.valueOf(100000));
        BudgetParticipant participant2 = createBudgetParticipant(tripJoin2, BigDecimal.valueOf(100000));

        SharedBudget sharedBudget = createSharedBudget(
                LocalDateTime.of(2024, 11, 1, 10, 0),
                "숙박비",
                PaymentMethod.CARD,
                CurrencyType.EUR,
                BigDecimal.valueOf(1450),
                CalculateType.EQUAL,
                List.of(participant1, participant2),
                trip
        );
        Budget savedBudget = budgetRepository.save(sharedBudget);
        em.flush();
        em.clear();

        // when
        SharedBudget foundBudget = (SharedBudget) budgetRepository.findById(savedBudget.getId()).orElseThrow();
        BudgetParticipant newParticipant1 = createBudgetParticipant(tripJoin1, BigDecimal.valueOf(150000));
        BudgetParticipant newParticipant2 = createBudgetParticipant(tripJoin2, BigDecimal.valueOf(150000));

        foundBudget.update(
                LocalDateTime.of(2024, 11, 1, 14, 0),
                "호텔 숙박비",
                PaymentMethod.CARD,
                CurrencyType.EUR,
                BigDecimal.valueOf(1500),
                CalculateType.EACH,
                List.of(newParticipant1, newParticipant2)
        );
        em.flush();
        em.clear();

        // then
        SharedBudget updatedBudget = (SharedBudget) budgetRepository.findById(savedBudget.getId()).orElse(null);
        assertThat(updatedBudget).isNotNull();
        assertThat(updatedBudget.getMemo()).isEqualTo("호텔 숙박비");
        assertThat(updatedBudget.getCalculateType()).isEqualTo(CalculateType.EACH);
        assertThat(updatedBudget.getExchangeRate()).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    @DisplayName("예산을 삭제한다")
    void deleteBudget() {
        // given
        Trip trip = createAndSaveTrip("삭제할 여행", LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 3));
        PersonalBudget personalBudget = createPersonalBudget(
                LocalDateTime.of(2024, 12, 1, 10, 0),
                "삭제할 예산",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        Budget savedBudget = budgetRepository.save(personalBudget);
        em.flush();
        em.clear();

        // when
        budgetRepository.deleteById(savedBudget.getId());
        em.flush();
        em.clear();

        // then
        Budget deletedBudget = budgetRepository.findById(savedBudget.getId()).orElse(null);
        assertThat(deletedBudget).isNull();
    }

    @Test
    @DisplayName("여행 ID로 공유 예산을 모두 조회한다")
    void findSharedBudgetExpenseAllByTripId() {
        // given
        Trip trip1 = createAndSaveTrip("여행1", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
        Trip trip2 = createAndSaveTrip("여행2", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));
        User user = createAndSaveUser("user5", "user5@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip1, user);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip2, user);

        BudgetParticipant participant1 = createBudgetParticipant(tripJoin1, BigDecimal.valueOf(50000));
        BudgetParticipant participant2 = createBudgetParticipant(tripJoin2, BigDecimal.valueOf(30000));

        SharedBudget sharedBudget1 = createSharedBudget(
                LocalDateTime.of(2024, 1, 1, 10, 0),
                "여행1 공유 예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                CalculateType.EQUAL,
                List.of(participant1),
                trip1
        );

        SharedBudget sharedBudget2 = createSharedBudget(
                LocalDateTime.of(2024, 1, 2, 10, 0),
                "여행1 공유 예산2",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                CalculateType.EQUAL,
                List.of(participant1),
                trip1
        );

        SharedBudget sharedBudget3 = createSharedBudget(
                LocalDateTime.of(2024, 2, 1, 10, 0),
                "여행2 공유 예산1",
                PaymentMethod.CARD,
                CurrencyType.USD,
                BigDecimal.valueOf(1300),
                CalculateType.EQUAL,
                List.of(participant2),
                trip2
        );

        budgetRepository.save(sharedBudget1);
        budgetRepository.save(sharedBudget2);
        budgetRepository.save(sharedBudget3);
        em.flush();
        em.clear();

        // when
        List<SharedBudget> sharedBudgets = budgetRepository.findSharedBudgetExpenseAllByTripId(trip1.getId());

        // then
        assertThat(sharedBudgets).hasSize(2);
        assertThat(sharedBudgets)
                .extracting(SharedBudget::getMemo)
                .containsExactlyInAnyOrder("여행1 공유 예산1", "여행1 공유 예산2");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 예산을 조회한다 - 생성일 오름차순")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        Trip trip = createAndSaveTrip("페이지 테스트 여행", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 1, 1, 10, 0),
                "예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 1, 2, 10, 0),
                "예산2",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );
        PersonalBudget budget3 = createPersonalBudget(
                LocalDateTime.of(2024, 1, 3, 10, 0),
                "예산3",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지 조회
        List<Budget> firstPage = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getMemo()).isEqualTo("예산1");
        assertThat(firstPage.get(1).getMemo()).isEqualTo("예산2");

        // when - 두 번째 페이지 조회
        Budget lastBudget = firstPage.getLast();
        List<Budget> secondPage = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastBudget.getCreatedAt().toString(),
                lastBudget.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getMemo()).isEqualTo("예산3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 예산을 조회한다 - 생성일 내림차순")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        Trip trip = createAndSaveTrip("내림차순 테스트 여행", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 10));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 3, 1, 10, 0),
                "예산A",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 3, 2, 10, 0),
                "예산B",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );
        PersonalBudget budget3 = createPersonalBudget(
                LocalDateTime.of(2024, 3, 3, 10, 0),
                "예산C",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(3);
        assertThat(budgets.get(0).getMemo()).isEqualTo("예산C");
        assertThat(budgets.get(1).getMemo()).isEqualTo("예산B");
        assertThat(budgets.get(2).getMemo()).isEqualTo("예산A");
    }

    @Test
    @DisplayName("키워드로 예산을 검색한다")
    void findAllByCursor_withKeyword() {
        // given
        Trip trip = createAndSaveTrip("검색 테스트 여행", LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 10));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 4, 1, 10, 0),
                "호텔 숙박비",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(100000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 4, 2, 10, 0),
                "식비",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(50000),
                trip
        );
        PersonalBudget budget3 = createPersonalBudget(
                LocalDateTime.of(2024, 4, 3, 10, 0),
                "호텔 조식",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                "호텔",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(2);
        assertThat(budgets)
                .extracting(Budget::getMemo)
                .containsExactly("호텔 숙박비", "호텔 조식");
    }

    @Test
    @DisplayName("여러 여행의 예산 중 특정 여행의 예산만 조회한다")
    void findAllByCursor_filterByTripId() {
        // given
        Trip trip1 = createAndSaveTrip("제주도", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 5));
        Trip trip2 = createAndSaveTrip("부산", LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 5, 1, 10, 0),
                "제주 호텔",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(100000),
                trip1
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 6, 1, 10, 0),
                "부산 호텔",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(80000),
                trip2
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip1.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(1);
        assertThat(budgets.getFirst().getMemo()).isEqualTo("제주 호텔");
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        Trip trip = createAndSaveTrip("커서 테스트", LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 7, 1, 10, 0),
                "예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 7, 2, 10, 0),
                "예산2",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(2);
        assertThat(budgets.get(0).getMemo()).isEqualTo("예산1");
        assertThat(budgets.get(1).getMemo()).isEqualTo("예산2");
    }

    @Test
    @DisplayName("tripId가 null인 경우 모든 여행의 예산을 조회한다")
    void findAllByCursor_withNullTripId() {
        // given
        Trip trip1 = createAndSaveTrip("여행A", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 5));
        Trip trip2 = createAndSaveTrip("여행B", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 8, 1, 10, 0),
                "여행A 예산",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip1
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 9, 1, 10, 0),
                "여행B 예산",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip2
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 예산을 조회한다")
    void findAllByCursor_withEmptyKeyword() {
        // given
        Trip trip = createAndSaveTrip("빈 키워드 테스트", LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 10, 1, 10, 0),
                "호텔",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 10, 2, 10, 0),
                "식비",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                "",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다")
    void findAllByCursor_withKeywordCaseInsensitive() {
        // given
        Trip trip = createAndSaveTrip("대소문자 테스트", LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 11, 1, 10, 0),
                "Hotel Breakfast",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 11, 2, 10, 0),
                "restaurant dinner",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                "hotel",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(1);
        assertThat(budgets.getFirst().getMemo()).isEqualTo("Hotel Breakfast");
    }

    @Test
    @DisplayName("내림차순 정렬 시 커서 기반 페이지네이션이 정상 작동한다")
    void findAllByCursor_withDescOrderAndCursor() {
        // given
        Trip trip = createAndSaveTrip("내림차순 커서 테스트", LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 10));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2024, 12, 1, 10, 0),
                "예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2024, 12, 2, 10, 0),
                "예산2",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );
        PersonalBudget budget3 = createPersonalBudget(
                LocalDateTime.of(2024, 12, 3, 10, 0),
                "예산3",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<Budget> firstPage = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getMemo()).isEqualTo("예산3");
        assertThat(firstPage.get(1).getMemo()).isEqualTo("예산2");

        // when - 두 번째 페이지
        Budget lastBudget = firstPage.getLast();
        List<Budget> secondPage = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                lastBudget.getCreatedAt().toString(),
                lastBudget.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getMemo()).isEqualTo("예산1");
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 예산들을 ID로 정렬한다 - 오름차순")
    void findAllByCursor_withSameCreatedAtOrderByIdAsc() {
        // given
        Trip trip = createAndSaveTrip("동일 시간 테스트", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5));
        LocalDateTime sameDateTime = LocalDateTime.of(2025, 1, 1, 10, 0);

        PersonalBudget budget1 = createPersonalBudget(
                sameDateTime,
                "예산A",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                sameDateTime,
                "예산B",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );
        PersonalBudget budget3 = createPersonalBudget(
                sameDateTime,
                "예산C",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(3);
        // ID 순으로 정렬되어야 함
        for (int i = 0; i < budgets.size() - 1; i++) {
            assertThat(budgets.get(i).getId()).isLessThan(budgets.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 예산들을 ID로 정렬한다 - 내림차순")
    void findAllByCursor_withSameCreatedAtOrderByIdDesc() {
        // given
        Trip trip = createAndSaveTrip("동일 시간 내림차순", LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 5));
        LocalDateTime sameDateTime = LocalDateTime.of(2025, 2, 1, 10, 0);

        PersonalBudget budget1 = createPersonalBudget(
                sameDateTime,
                "예산X",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                sameDateTime,
                "예산Y",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );
        PersonalBudget budget3 = createPersonalBudget(
                sameDateTime,
                "예산Z",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(3);
        // ID 역순으로 정렬되어야 함
        for (int i = 0; i < budgets.size() - 1; i++) {
            assertThat(budgets.get(i).getId()).isGreaterThan(budgets.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("limit 크기만큼만 결과를 반환한다")
    void findAllByCursor_withLimit() {
        // given
        Trip trip = createAndSaveTrip("limit 테스트", LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 10));

        for (int i = 1; i <= 10; i++) {
            PersonalBudget budget = createPersonalBudget(
                    LocalDateTime.of(2025, 3, i, 10, 0),
                    "예산" + i,
                    PaymentMethod.CARD,
                    CurrencyType.KRW,
                    BigDecimal.valueOf(1),
                    BigDecimal.valueOf(10000 * i),
                    trip
            );
            budgetRepository.save(budget);
        }
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                3
        );

        // then
        assertThat(budgets).hasSize(3);
    }

    @Test
    @DisplayName("키워드와 tripId를 함께 사용하여 필터링한다")
    void findAllByCursor_withKeywordAndTripId() {
        // given
        Trip trip1 = createAndSaveTrip("서울 여행", LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 5));
        Trip trip2 = createAndSaveTrip("대전 여행", LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2025, 4, 1, 10, 0),
                "서울 호텔",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(100000),
                trip1
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2025, 4, 2, 10, 0),
                "서울 식비",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(50000),
                trip1
        );
        PersonalBudget budget3 = createPersonalBudget(
                LocalDateTime.of(2025, 5, 1, 10, 0),
                "대전 호텔",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(80000),
                trip2
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip1.getId(),
                "호텔",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(1);
        assertThat(budgets.getFirst().getMemo()).isEqualTo("서울 호텔");
    }

    @Test
    @DisplayName("결과가 없는 경우 빈 리스트를 반환한다")
    void findAllByCursor_withNoResults() {
        // given
        Trip trip = createAndSaveTrip("빈 결과 테스트", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 5));

        PersonalBudget budget = createPersonalBudget(
                LocalDateTime.of(2025, 6, 1, 10, 0),
                "식비",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        budgetRepository.save(budget);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                "호텔",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).isEmpty();
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 같은 createdAt을 가진 항목이 있을 때 정확히 다음 페이지를 가져온다")
    void findAllByCursor_withSameCreatedAtInCursor() {
        // given
        Trip trip = createAndSaveTrip("같은 시간 커서", LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5));
        LocalDateTime sameDateTime = LocalDateTime.of(2025, 7, 1, 10, 0);

        PersonalBudget budget1 = createPersonalBudget(
                sameDateTime,
                "예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                sameDateTime,
                "예산2",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );
        PersonalBudget budget3 = createPersonalBudget(
                sameDateTime,
                "예산3",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Budget> firstPage = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 다음 페이지
        Budget lastBudget = firstPage.getLast();
        List<Budget> secondPage = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                lastBudget.getCreatedAt().toString(),
                lastBudget.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getId()).isGreaterThan(lastBudget.getId());
    }

    @Test
    @DisplayName("cursor가 null이지만 after가 있는 경우 - after는 무시된다")
    void findAllByCursor_withNullCursorButAfterExists() {
        // given
        Trip trip = createAndSaveTrip("커서 null 테스트", LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2025, 8, 1, 10, 0),
                "예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2025, 8, 2, 10, 0),
                "예산2",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                999L,
                10
        );

        // then - cursor가 null이므로 모든 데이터 조회
        assertThat(budgets).hasSize(2);
    }

    @Test
    @DisplayName("cursor만 있고 after가 null인 경우 - 조건이 적용되지 않는다")
    void findAllByCursor_withCursorButNullAfter() {
        // given
        Trip trip = createAndSaveTrip("after null 테스트", LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2025, 9, 1, 10, 0),
                "예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2025, 9, 2, 10, 0),
                "예산2",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                LocalDateTime.of(2025, 9, 1, 10, 0).toString(),
                null,
                10
        );

        // then - after가 null이므로 모든 데이터 조회
        assertThat(budgets).hasSize(2);
    }

    @Test
    @DisplayName("공유 예산과 개인 예산을 함께 조회한다")
    void findAllByCursor_withMixedBudgetTypes() {
        // given
        Trip trip = createAndSaveTrip("혼합 타입 테스트", LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 5));
        User user1 = createAndSaveUser("mixed1", "mixed1@email.com");
        User user2 = createAndSaveUser("mixed2", "mixed2@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        PersonalBudget personalBudget = createPersonalBudget(
                LocalDateTime.of(2025, 10, 1, 10, 0),
                "개인 예산",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );

        BudgetParticipant participant1 = createBudgetParticipant(tripJoin1, BigDecimal.valueOf(50000));
        BudgetParticipant participant2 = createBudgetParticipant(tripJoin2, BigDecimal.valueOf(50000));
        SharedBudget sharedBudget = createSharedBudget(
                LocalDateTime.of(2025, 10, 2, 10, 0),
                "공유 예산",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                CalculateType.EQUAL,
                List.of(participant1, participant2),
                trip
        );

        budgetRepository.save(personalBudget);
        budgetRepository.save(sharedBudget);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(2);
        assertThat(budgets.get(0)).isInstanceOf(PersonalBudget.class);
        assertThat(budgets.get(1)).isInstanceOf(SharedBudget.class);
    }

    @Test
    @DisplayName("여러 키워드 조합으로 검색한다")
    void findAllByCursor_withMultipleKeywords() {
        // given
        Trip trip = createAndSaveTrip("복합 키워드 테스트", LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2025, 11, 1, 10, 0),
                "호텔 조식 비용",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2025, 11, 2, 10, 0),
                "호텔 숙박 비용",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(100000),
                trip
        );
        PersonalBudget budget3 = createPersonalBudget(
                LocalDateTime.of(2025, 11, 3, 10, 0),
                "레스토랑 저녁 식사",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(50000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        budgetRepository.save(budget3);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                "조식",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(1);
        assertThat(budgets.getFirst().getMemo()).contains("조식");
    }

    @Test
    @DisplayName("direction이 ASC가 아닌 대소문자 혼합일 때도 정상 작동한다")
    void findAllByCursor_withMixedCaseDirection() {
        // given
        Trip trip = createAndSaveTrip("대소문자 direction", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                "예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2026, 1, 2, 10, 0),
                "예산2",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgetsAsc = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "AsC",
                null,
                null,
                10
        );

        List<Budget> budgetsDesc = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "DeSc",
                null,
                null,
                10
        );

        // then
        assertThat(budgetsAsc).hasSize(2);
        assertThat(budgetsAsc.getFirst().getMemo()).isEqualTo("예산1");
        assertThat(budgetsDesc).hasSize(2);
        assertThat(budgetsDesc.getFirst().getMemo()).isEqualTo("예산2");
    }

    @Test
    @DisplayName("특수문자가 포함된 키워드로 검색한다")
    void findAllByCursor_withSpecialCharactersInKeyword() {
        // given
        Trip trip = createAndSaveTrip("특수문자 테스트", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2026, 2, 1, 10, 0),
                "호텔 (5성급) 숙박",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(200000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2026, 2, 2, 10, 0),
                "카페 라떼 & 케이크",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(15000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                "5성급",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(1);
        assertThat(budgets.getFirst().getMemo()).contains("5성급");
    }

    @Test
    @DisplayName("매우 큰 limit 값으로 조회한다")
    void findAllByCursor_withVeryLargeLimit() {
        // given
        Trip trip = createAndSaveTrip("큰 limit", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5));

        PersonalBudget budget1 = createPersonalBudget(
                LocalDateTime.of(2026, 3, 1, 10, 0),
                "예산1",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(10000),
                trip
        );
        PersonalBudget budget2 = createPersonalBudget(
                LocalDateTime.of(2026, 3, 2, 10, 0),
                "예산2",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(20000),
                trip
        );

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                1000000
        );

        // then
        assertThat(budgets).hasSize(2);
    }

    @Test
    @DisplayName("같은 여행에 여러 타입의 예산이 섞여 있을 때 키워드로 필터링한다")
    void findAllByCursor_withKeywordOnMixedTypes() {
        // given
        Trip trip = createAndSaveTrip("혼합 타입 키워드", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5));
        User user1 = createAndSaveUser("keyword1", "keyword1@email.com");
        User user2 = createAndSaveUser("keyword2", "keyword2@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        PersonalBudget personalBudget = createPersonalBudget(
                LocalDateTime.of(2026, 4, 1, 10, 0),
                "개인 교통비",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(50000),
                trip
        );

        BudgetParticipant participant1 = createBudgetParticipant(tripJoin1, BigDecimal.valueOf(100000));
        BudgetParticipant participant2 = createBudgetParticipant(tripJoin2, BigDecimal.valueOf(100000));
        SharedBudget sharedBudget = createSharedBudget(
                LocalDateTime.of(2026, 4, 2, 10, 0),
                "공유 교통비",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                CalculateType.EQUAL,
                List.of(participant1, participant2),
                trip
        );

        budgetRepository.save(personalBudget);
        budgetRepository.save(sharedBudget);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                "교통비",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(2);
        assertThat(budgets)
                .extracting(Budget::getMemo)
                .containsExactly("개인 교통비", "공유 교통비");
    }

    @Test
    @DisplayName("페이지네이션 경계값 - 정확히 limit만큼의 데이터가 있을 때")
    void findAllByCursor_withExactLimitData() {
        // given
        Trip trip = createAndSaveTrip("경계값 테스트", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 5));

        for (int i = 1; i <= 5; i++) {
            PersonalBudget budget = createPersonalBudget(
                    LocalDateTime.of(2026, 5, i, 10, 0),
                    "예산" + i,
                    PaymentMethod.CARD,
                    CurrencyType.KRW,
                    BigDecimal.valueOf(1),
                    BigDecimal.valueOf(10000 * i),
                    trip
            );
            budgetRepository.save(budget);
        }
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                5
        );

        // then
        assertThat(budgets).hasSize(5);
    }

    @Test
    @DisplayName("공유 예산만 키워드로 필터링한다")
    void findAllByCursor_withKeywordForSharedBudgetOnly() {
        // given
        Trip trip = createAndSaveTrip("공유 예산 필터", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5));
        User user1 = createAndSaveUser("shared1", "shared1@email.com");
        User user2 = createAndSaveUser("shared2", "shared2@email.com");
        TripJoin tripJoin1 = createAndSaveTripJoin(trip, user1);
        TripJoin tripJoin2 = createAndSaveTripJoin(trip, user2);

        PersonalBudget personalBudget = createPersonalBudget(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                "개인 식비",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(30000),
                trip
        );

        BudgetParticipant participant1 = createBudgetParticipant(tripJoin1, BigDecimal.valueOf(75000));
        BudgetParticipant participant2 = createBudgetParticipant(tripJoin2, BigDecimal.valueOf(75000));
        SharedBudget sharedBudget = createSharedBudget(
                LocalDateTime.of(2026, 6, 2, 10, 0),
                "공유 렌터카",
                PaymentMethod.CARD,
                CurrencyType.KRW,
                BigDecimal.valueOf(1),
                CalculateType.EQUAL,
                List.of(participant1, participant2),
                trip
        );

        budgetRepository.save(personalBudget);
        budgetRepository.save(sharedBudget);
        em.flush();
        em.clear();

        // when
        List<Budget> budgets = budgetRepository.findAllByCursor(
                trip.getId(),
                "렌터카",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(budgets).hasSize(1);
        assertThat(budgets.getFirst()).isInstanceOf(SharedBudget.class);
        assertThat(budgets.getFirst().getMemo()).isEqualTo("공유 렌터카");
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

    private PersonalBudget createPersonalBudget(
            LocalDateTime dateTime,
            String memo,
            PaymentMethod paymentMethod,
            CurrencyType currencyType,
            BigDecimal exchangeRate,
            BigDecimal totalAmount,
            Trip trip
    ) {
        return PersonalBudget.createPersonalBudget(
                dateTime,
                memo,
                paymentMethod,
                currencyType,
                exchangeRate,
                totalAmount,
                trip
        );
    }

    private SharedBudget createSharedBudget(
            LocalDateTime dateTime,
            String memo,
            PaymentMethod paymentMethod,
            CurrencyType currencyType,
            BigDecimal exchangeRate,
            CalculateType calculateType,
            List<BudgetParticipant> participants,
            Trip trip
    ) {
        return SharedBudget.createSharedBudget(
                dateTime,
                memo,
                paymentMethod,
                currencyType,
                exchangeRate,
                calculateType,
                participants,
                trip
        );
    }

    private BudgetParticipant createBudgetParticipant(TripJoin tripJoin, BigDecimal amount) {
        return BudgetParticipant.createBudgetParticipant(tripJoin, amount);
    }
}
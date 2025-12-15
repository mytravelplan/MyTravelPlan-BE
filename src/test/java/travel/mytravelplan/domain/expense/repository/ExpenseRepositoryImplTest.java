package travel.mytravelplan.domain.expense.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.dto.ExpenseRatioDto;
import travel.mytravelplan.domain.expense.dto.ExpenseStatisticsDto;
import travel.mytravelplan.domain.expense.entity.Expense;
import travel.mytravelplan.domain.expense.entity.ExpenseParticipant;
import travel.mytravelplan.domain.expense.entity.PersonalExpense;
import travel.mytravelplan.domain.expense.entity.SharedExpense;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.GroupByType;
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

@DisplayName("지출 레포지토리 구현체 테스트")
class ExpenseRepositoryImplTest extends RepositoryTestSupport {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripJoinRepository tripJoinRepository;

    @Test
    @DisplayName("스케줄 ID로 지출을 조회한다")
    void findAllByCursor_byScheduleId() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule1 = createSchedule("스케줄1", trip);
        Schedule schedule2 = createSchedule("스케줄2", trip);

        createAndSavePersonalExpense("식비", ExpenseCategory.FOOD, schedule1, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule1, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("숙박비", ExpenseCategory.ACCOMMODATION, schedule2, BigDecimal.valueOf(30000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule1.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(2);
        assertThat(expenses)
                .extracting(Expense::getMemo)
                .containsExactlyInAnyOrder("식비", "교통비");
    }

    @Test
    @DisplayName("키워드로 지출을 검색한다")
    void findAllByCursor_byKeyword() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케쥴", trip);

        createAndSavePersonalExpense("점심 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("저녁 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(5000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                "식비",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(2);
        assertThat(expenses)
                .extracting(Expense::getMemo)
                .containsExactlyInAnyOrder("점심 식비", "저녁 식비");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 지출을 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("지출1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("지출2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("지출3", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(30000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(3);
        assertThat(expenses.get(0).getMemo()).isEqualTo("지출3");
        assertThat(expenses.get(1).getMemo()).isEqualTo("지출2");
        assertThat(expenses.get(2).getMemo()).isEqualTo("지출1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 지출을 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("지출1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("지출2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("지출3", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(30000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(3);
        assertThat(expenses.get(0).getMemo()).isEqualTo("지출1");
        assertThat(expenses.get(1).getMemo()).isEqualTo("지출2");
        assertThat(expenses.get(2).getMemo()).isEqualTo("지출3");
    }

    @Test
    @DisplayName("limit 개수만큼 지출을 조회한다")
    void findAllByCursor_withLimit() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("지출1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("지출2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("지출3", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(30000));
        createAndSavePersonalExpense("지출4", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(40000));
        createAndSavePersonalExpense("지출5", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(50000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(expenses).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 지출을 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("지출1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("지출2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("지출3", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(30000));

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getMemo()).isEqualTo("지출3");
        assertThat(firstPage.get(1).getMemo()).isEqualTo("지출2");

        // when - 두 번째 페이지 조회
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getMemo()).isEqualTo("지출1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 지출을 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("지출1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("지출2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("지출3", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(30000));

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getMemo()).isEqualTo("지출1");
        assertThat(firstPage.get(1).getMemo()).isEqualTo("지출2");

        // when - 두 번째 페이지 조회
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "asc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getMemo()).isEqualTo("지출3");
    }

    @Test
    @DisplayName("스케줄 ID와 키워드로 지출을 조회한다")
    void findAllByCursor_byScheduleIdAndKeyword() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule1 = createSchedule("스케줄1", trip);
        Schedule schedule2 = createSchedule("스케줄2", trip);

        createAndSavePersonalExpense("점심 식비", ExpenseCategory.FOOD, schedule1, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule1, BigDecimal.valueOf(5000));
        createAndSavePersonalExpense("저녁 식비", ExpenseCategory.FOOD, schedule1, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("아침 식비", ExpenseCategory.FOOD, schedule2, BigDecimal.valueOf(8000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule1.getId(),
                "식비",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(2);
        assertThat(expenses)
                .extracting(Expense::getMemo)
                .containsExactlyInAnyOrder("점심 식비", "저녁 식비");
    }

    @Test
    @DisplayName("키워드와 커서를 함께 사용하여 페이지네이션할 수 있다 - 내림차순")
    void findAllByCursor_withKeywordAndCursor_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("점심 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("저녁 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(5000));
        createAndSavePersonalExpense("아침 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(8000));
        createAndSavePersonalExpense("간식 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(3000));

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                "식비",
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getMemo()).isEqualTo("간식 식비");
        assertThat(firstPage.get(1).getMemo()).isEqualTo("아침 식비");

        // when - 두 번째 페이지 조회
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                "식비",
                "createdAt",
                "desc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Expense::getMemo)
                .containsExactly("저녁 식비", "점심 식비");
    }

    @Test
    @DisplayName("키워드와 커서를 함께 사용하여 페이지네이션할 수 있다 - 오름차순")
    void findAllByCursor_withKeywordAndCursor_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("점심 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("저녁 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(5000));
        createAndSavePersonalExpense("아침 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(8000));

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                "식비",
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Expense::getMemo)
                .containsExactly("점심 식비", "저녁 식비");

        // when - 두 번째 페이지 조회
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                "식비",
                "createdAt",
                "asc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getMemo()).isEqualTo("아침 식비");
    }

    @Test
    @DisplayName("여러 페이지를 순차적으로 조회한다 - 내림차순")
    void findAllByCursor_multiplePages_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        for (int i = 1; i <= 10; i++) {
            createAndSavePersonalExpense("지출" + i, ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(i * 1000));
        }

        em.flush();
        em.clear();

        // when & then - 첫 번째 페이지
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );
        assertThat(firstPage).hasSize(3);
        assertThat(firstPage.get(0).getMemo()).isEqualTo("지출10");
        assertThat(firstPage.get(2).getMemo()).isEqualTo("지출8");

        // when & then - 두 번째 페이지
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                3
        );
        assertThat(secondPage).hasSize(3);
        assertThat(secondPage.get(0).getMemo()).isEqualTo("지출7");
        assertThat(secondPage.get(2).getMemo()).isEqualTo("지출5");

        // when & then - 세 번째 페이지
        lastExpense = secondPage.getLast();
        List<Expense> thirdPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                3
        );
        assertThat(thirdPage).hasSize(3);
        assertThat(thirdPage.get(0).getMemo()).isEqualTo("지출4");
        assertThat(thirdPage.get(2).getMemo()).isEqualTo("지출2");

        // when & then - 네 번째 페이지 (마지막)
        lastExpense = thirdPage.getLast();
        List<Expense> fourthPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                3
        );
        assertThat(fourthPage).hasSize(1);
        assertThat(fourthPage.getFirst().getMemo()).isEqualTo("지출1");
    }

    @Test
    @DisplayName("여러 페이지를 순차적으로 조회한다 - 오름차순")
    void findAllByCursor_multiplePages_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        for (int i = 1; i <= 7; i++) {
            createAndSavePersonalExpense("지출" + i, ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(i * 1000));
        }

        em.flush();
        em.clear();

        // when & then - 첫 번째 페이지
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                3
        );
        assertThat(firstPage).hasSize(3);
        assertThat(firstPage)
                .extracting(Expense::getMemo)
                .containsExactly("지출1", "지출2", "지출3");

        // when & then - 두 번째 페이지
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "asc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                3
        );
        assertThat(secondPage).hasSize(3);
        assertThat(secondPage)
                .extracting(Expense::getMemo)
                .containsExactly("지출4", "지출5", "지출6");

        // when & then - 세 번째 페이지 (마지막)
        lastExpense = secondPage.getLast();
        List<Expense> thirdPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "asc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                3
        );
        assertThat(thirdPage).hasSize(1);
        assertThat(thirdPage.getFirst().getMemo()).isEqualTo("지출7");
    }

    @Test
    @DisplayName("마지막 페이지 이후에는 빈 리스트를 반환한다 - 내림차순")
    void findAllByCursor_emptyAfterLastPage_desc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("지출1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("지출2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));

        em.flush();
        em.clear();

        // when - 모든 데이터 조회
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // when - 마지막 데이터 이후 조회
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                10
        );

        // then
        assertThat(secondPage).isEmpty();
    }

    @Test
    @DisplayName("마지막 페이지 이후에는 빈 리스트를 반환한다 - 오름차순")
    void findAllByCursor_emptyAfterLastPage_asc() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("지출1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("지출2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));

        em.flush();
        em.clear();

        // when - 모든 데이터 조회
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // when - 마지막 데이터 이후 조회
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "asc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                10
        );

        // then
        assertThat(secondPage).isEmpty();
    }

    @Test
    @DisplayName("지출이 없는 경우 빈 리스트를 반환한다")
    void findAllByCursor_emptyResult() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).isEmpty();
    }

    @Test
    @DisplayName("키워드로 검색했을 때 결과가 없으면 빈 리스트를 반환한다")
    void findAllByCursor_emptyResultWithKeyword() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(5000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                "숙박비",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).isEmpty();
    }

    @Test
    @DisplayName("limit이 1일 때 정확히 1개의 항목만 조회한다")
    void findAllByCursor_withLimitOne() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("지출1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("지출2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("지출3", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(30000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        // then
        assertThat(expenses).hasSize(1);
        assertThat(expenses.getFirst().getMemo()).isEqualTo("지출3");
    }

    @Test
    @DisplayName("대용량 데이터에서 커서 페이지네이션이 정상 동작한다")
    void findAllByCursor_largeDataset() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        // 100개의 지출 생성
        for (int i = 1; i <= 100; i++) {
            createAndSavePersonalExpense("지출" + i, ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(i * 1000));
        }

        em.flush();
        em.clear();

        // when - 여러 페이지 조회
        int totalFetched = 0;
        String cursorValue = null;
        Long cursorId = null;
        int pageSize = 10;

        for (int page = 0; page < 10; page++) {
            List<Expense> expenses = expenseRepository.findAllByCursor(
                    schedule.getId(),
                    null,
                    "createdAt",
                    "desc",
                    cursorValue,
                    cursorId,
                    pageSize
            );

            totalFetched += expenses.size();

            if (expenses.isEmpty()) {
                break;
            }

            Expense lastExpense = expenses.getLast();
            cursorValue = lastExpense.getCreatedAt().toString();
            cursorId = lastExpense.getId();
        }

        // then - 모든 데이터가 조회되었는지 확인
        assertThat(totalFetched).isEqualTo(100);
    }

    @Test
    @DisplayName("존재하지 않는 스케줄 ID로 조회하면 빈 리스트를 반환한다")
    void findAllByCursor_nonExistentScheduleId() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                999999L,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).isEmpty();
    }

    @Test
    @DisplayName("다른 스케줄의 지출은 조회되지 않는다")
    void findAllByCursor_onlyReturnsExpensesForSpecificSchedule() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule1 = createSchedule("스케줄1", trip);
        Schedule schedule2 = createSchedule("스케줄2", trip);

        createAndSavePersonalExpense("스케줄1 식비", ExpenseCategory.FOOD, schedule1, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("스케줄1 교통비", ExpenseCategory.TRANSPORTATION, schedule1, BigDecimal.valueOf(5000));
        createAndSavePersonalExpense("스케줄2 식비", ExpenseCategory.FOOD, schedule2, BigDecimal.valueOf(20000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule1.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(2);
        assertThat(expenses)
                .extracting(Expense::getMemo)
                .containsExactlyInAnyOrder("스케줄1 식비", "스케줄1 교통비");
    }

    @Test
    @DisplayName("여러 카테고리가 혼합된 경우에도 정상적으로 조회된다")
    void findAllByCursor_withMultipleCategories() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(5000));
        createAndSavePersonalExpense("숙박비", ExpenseCategory.ACCOMMODATION, schedule, BigDecimal.valueOf(50000));
        createAndSavePersonalExpense("쇼핑", ExpenseCategory.SHOPPING, schedule, BigDecimal.valueOf(30000));
        createAndSavePersonalExpense("오락", ExpenseCategory.ENTERTAINMENT, schedule, BigDecimal.valueOf(20000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(5);
    }

    @Test
    @DisplayName("개인 지출과 공유 지출이 혼합된 경우 모두 조회된다")
    void findAllByCursor_withMixedExpenseTypes() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);
        TripJoin tripJoin1 = createTripJoin(trip, user1);
        TripJoin tripJoin2 = createTripJoin(trip, user2);

        createAndSavePersonalExpense("개인 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSaveSharedExpense("공유 교통비", ExpenseCategory.TRANSPORTATION, schedule, tripJoin1,
                List.of(
                        createExpenseParticipant(tripJoin1, BigDecimal.valueOf(5000)),
                        createExpenseParticipant(tripJoin2, BigDecimal.valueOf(5000))
                ));
        createAndSavePersonalExpense("개인 쇼핑", ExpenseCategory.SHOPPING, schedule, BigDecimal.valueOf(30000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(3);
        assertThat(expenses)
                .extracting(Expense::getMemo)
                .containsExactlyInAnyOrder("개인 식비", "공유 교통비", "개인 쇼핑");
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자 구분 없이 검색된다")
    void findAllByCursor_keywordSearchIgnoreCase() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("FOOD 식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("food 간식", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(5000));
        createAndSavePersonalExpense("Food 저녁", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(3000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                "food",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(3);
        assertThat(expenses)
                .extracting(Expense::getMemo)
                .containsExactlyInAnyOrder("FOOD 식비", "food 간식", "Food 저녁");
    }

    @Test
    @DisplayName("부분 문자열 검색이 정상적으로 동작한다")
    void findAllByCursor_partialStringSearch() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("점심식사비용", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("저녁식사", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("식사 후 커피", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(5000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(3000));

        em.flush();
        em.clear();

        // when
        List<Expense> expenses = expenseRepository.findAllByCursor(
                schedule.getId(),
                "식사",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(expenses).hasSize(3);
        assertThat(expenses)
                .extracting(Expense::getMemo)
                .containsExactlyInAnyOrder("점심식사비용", "저녁식사", "식사 후 커피");
    }

    @Test
    @DisplayName("동일한 생성시간을 가진 항목들도 ID로 정확히 구분하여 페이지네이션된다")
    void findAllByCursor_withSameCreatedAt() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        // 같은 시간에 여러 지출 생성 (테스트 환경에서는 거의 동시에 생성됨)
        for (int i = 1; i <= 5; i++) {
            createAndSavePersonalExpense("지출" + i, ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(i * 1000));
        }

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Expense> firstPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(3);

        // when - 두 번째 페이지
        Expense lastExpense = firstPage.getLast();
        List<Expense> secondPage = expenseRepository.findAllByCursor(
                schedule.getId(),
                null,
                "createdAt",
                "desc",
                lastExpense.getCreatedAt().toString(),
                lastExpense.getId(),
                3
        );

        // then - 두 번째 페이지 검증 (중복 없이 다음 항목들이 조회되어야 함)
        assertThat(secondPage).hasSize(2);
        assertThat(firstPage)
                .extracting(Expense::getId)
                .doesNotContainAnyElementsOf(secondPage.stream()
                        .map(Expense::getId)
                        .toList());
    }

    @Test
    @DisplayName("개인 지출 통계를 조회한다")
    void getExpenseStatistics_personal() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("식비1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000));
        createAndSavePersonalExpense("식비2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(15000));

        em.flush();
        em.clear();

        // when
        ExpenseStatisticsDto statistics = expenseRepository.getExpenseStatistics(
                trip.getId(),
                ExpenseType.PERSONAL,
                GroupByType.CATEGORY,
                null
        );

        // then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(45000));
        assertThat(statistics.getStatistics()).hasSize(2);
    }

    @Test
    @DisplayName("공유 지출 통계를 조회한다")
    void getExpenseStatistics_shared() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);
        TripJoin tripJoin1 = createTripJoin(trip, user1);
        TripJoin tripJoin2 = createTripJoin(trip, user2);

        createAndSaveSharedExpense("식비1", ExpenseCategory.FOOD, schedule, tripJoin1,
                List.of(
                        createExpenseParticipant(tripJoin1, BigDecimal.valueOf(15000)),
                        createExpenseParticipant(tripJoin2, BigDecimal.valueOf(15000))
                ));
        createAndSaveSharedExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, tripJoin1,
                List.of(
                        createExpenseParticipant(tripJoin1, BigDecimal.valueOf(10000)),
                        createExpenseParticipant(tripJoin2, BigDecimal.valueOf(10000))
                ));

        em.flush();
        em.clear();

        // when
        ExpenseStatisticsDto statistics = expenseRepository.getExpenseStatistics(
                trip.getId(),
                ExpenseType.SHARED,
                GroupByType.CATEGORY,
                null
        );

        // then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(statistics.getStatistics()).hasSize(2);
    }

    @Test
    @DisplayName("특정 날짜의 개인 지출 통계를 조회한다")
    void getExpenseStatistics_personal_withDate() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        LocalDate targetDate = LocalDate.of(2024, 1, 5);
        createAndSavePersonalExpenseWithDateTime("식비1", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(10000),
                LocalDateTime.of(2024, 1, 5, 12, 0));
        createAndSavePersonalExpenseWithDateTime("식비2", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(20000),
                LocalDateTime.of(2024, 1, 5, 18, 0));
        createAndSavePersonalExpenseWithDateTime("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(15000),
                LocalDateTime.of(2024, 1, 6, 10, 0));

        em.flush();
        em.clear();

        // when
        ExpenseStatisticsDto statistics = expenseRepository.getExpenseStatistics(
                trip.getId(),
                ExpenseType.PERSONAL,
                GroupByType.CATEGORY,
                targetDate
        );

        // then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        assertThat(statistics.getStatistics()).hasSize(1);
        assertThat(statistics.getStatistics().getFirst().getExpenseCategory()).isEqualTo(ExpenseCategory.FOOD);
    }

    @Test
    @DisplayName("카테고리별 개인 지출 비율을 계산한다")
    void getExpenseStatistics_personal_withRatio() {
        // given
        Trip trip = createTrip("여행 계획");
        Schedule schedule = createSchedule("스케줄", trip);

        createAndSavePersonalExpense("식비", ExpenseCategory.FOOD, schedule, BigDecimal.valueOf(30000));
        createAndSavePersonalExpense("교통비", ExpenseCategory.TRANSPORTATION, schedule, BigDecimal.valueOf(20000));
        createAndSavePersonalExpense("쇼핑", ExpenseCategory.SHOPPING, schedule, BigDecimal.valueOf(50000));

        em.flush();
        em.clear();

        // when
        ExpenseStatisticsDto statistics = expenseRepository.getExpenseStatistics(
                trip.getId(),
                ExpenseType.PERSONAL,
                GroupByType.CATEGORY,
                null
        );

        // then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(statistics.getStatistics()).hasSize(3);

        ExpenseRatioDto foodRatio = statistics.getStatistics().stream()
                .filter(s -> s.getExpenseCategory() == ExpenseCategory.FOOD)
                .findFirst()
                .orElse(null);
        assertThat(foodRatio).isNotNull();
        assertThat(foodRatio.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
    }

    @Test
    @DisplayName("여행에 지출이 없는 경우 빈 통계를 반환한다")
    void getExpenseStatistics_empty() {
        // given
        Trip trip = createTrip("여행 계획");

        // when
        ExpenseStatisticsDto statistics = expenseRepository.getExpenseStatistics(
                trip.getId(),
                ExpenseType.PERSONAL,
                GroupByType.CATEGORY,
                null
        );

        // then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(statistics.getStatistics()).hasSize(0);
    }

    // TestFixture 메서드들
    private User createUser(String username, String email) {
        User user = User.createUser(
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
        return userRepository.save(user);
    }

    private Trip createTrip(String title) {
        Trip trip = Trip.createTrip(
                title,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 10),
                "https://example.com/image.jpg",
                Set.of(Country.JP)
        );
        return tripRepository.save(trip);
    }

    private Schedule createSchedule(String title, Trip trip) {
        Schedule schedule = Schedule.createSchedule(
                title,
                LocalDateTime.of(2024, 1, 5, 10, 0),
                LocalDateTime.of(2024, 1, 5, 12, 0),
                "메모",
                1L,
                null,
                trip,
                BigDecimal.valueOf(4.5)
        );
        return scheduleRepository.save(schedule);
    }

    private TripJoin createTripJoin(Trip trip, User user) {
        TripJoin tripJoin = TripJoin.createTripJoin(trip, user);
        return tripJoinRepository.save(tripJoin);
    }

    private PersonalExpense createPersonalExpense(String memo, ExpenseCategory category, Schedule schedule, BigDecimal amount) {
        return PersonalExpense.createPersonalExpense(
                LocalDateTime.of(2024, 1, 5, 12, 0),
                memo,
                PaymentMethod.CARD,
                category,
                CurrencyType.KRW,
                BigDecimal.ONE,
                amount,
                schedule
        );
    }

    private PersonalExpense createPersonalExpenseWithDateTime(String memo, ExpenseCategory category, Schedule schedule, BigDecimal amount, LocalDateTime dateTime) {
        return PersonalExpense.createPersonalExpense(
                dateTime,
                memo,
                PaymentMethod.CARD,
                category,
                CurrencyType.KRW,
                BigDecimal.ONE,
                amount,
                schedule
        );
    }

    private PersonalExpense createAndSavePersonalExpense(String memo, ExpenseCategory category, Schedule schedule, BigDecimal amount) {
        PersonalExpense expense = createPersonalExpense(memo, category, schedule, amount);
        return expenseRepository.save(expense);
    }

    private void createAndSavePersonalExpenseWithDateTime(String memo, ExpenseCategory category, Schedule schedule, BigDecimal amount, LocalDateTime dateTime) {
        PersonalExpense expense = createPersonalExpenseWithDateTime(memo, category, schedule, amount, dateTime);
        expenseRepository.save(expense);
    }

    private SharedExpense createSharedExpense(String memo, ExpenseCategory category, Schedule schedule, TripJoin payer, List<ExpenseParticipant> participants) {
        return SharedExpense.createSharedExpense(
                LocalDateTime.of(2024, 1, 5, 12, 0),
                memo,
                PaymentMethod.CARD,
                category,
                CurrencyType.KRW,
                BigDecimal.ONE,
                CalculateType.EQUAL,
                payer,
                participants,
                schedule
        );
    }

    private void createAndSaveSharedExpense(String memo, ExpenseCategory category, Schedule schedule, TripJoin payer, List<ExpenseParticipant> participants) {
        SharedExpense expense = createSharedExpense(memo, category, schedule, payer, participants);
        expenseRepository.save(expense);
    }

    private ExpenseParticipant createExpenseParticipant(TripJoin tripJoin, BigDecimal amount) {
        return ExpenseParticipant.createExpenseParticipant(tripJoin, amount);
    }
}


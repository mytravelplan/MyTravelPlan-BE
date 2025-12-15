package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.expense.entity.SharedExpense;
import travel.mytravelplan.domain.expense.enums.ExpenseCategory;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.entity.Expense;
import travel.mytravelplan.domain.expense.entity.ExpenseParticipant;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.expense.exception.ExpenseException;
import travel.mytravelplan.domain.expense.repository.ExpenseParticipantRepository;
import travel.mytravelplan.domain.expense.repository.ExpenseRepository;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.schedule.exception.ScheduleException;
import travel.mytravelplan.domain.schedule.repository.ScheduleRepository;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.global.error.code.ExpenseErrorCode;
import travel.mytravelplan.global.error.code.ScheduleErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Profile("local")
@Component
@Order(5)
@RequiredArgsConstructor
public class ExpenseInitializer implements ApplicationRunner {
    private final ExpenseRepository expenseRepository;
    private final ScheduleRepository scheduleRepository;
    private final TripJoinRepository tripJoinRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Schedule schedule = scheduleRepository.findById(1L)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        TripJoin tripJoin1 = tripJoinRepository.findById(1L)
                .orElseThrow(() -> new ExpenseException(ExpenseErrorCode.EXPENSE_PARTICIPANT_NOT_FOUND));

        TripJoin tripJoin2 = tripJoinRepository.findById(2L)
                .orElseThrow(() -> new ExpenseException(ExpenseErrorCode.EXPENSE_PARTICIPANT_NOT_FOUND));

        TripJoin tripJoin3 = tripJoinRepository.findById(3L)
                .orElseThrow(() -> new ExpenseException(ExpenseErrorCode.EXPENSE_PARTICIPANT_NOT_FOUND));

        // 초밥
        ExpenseParticipant expenseParticipant1 = ExpenseParticipant.createExpenseParticipant(
                tripJoin1,
                new BigDecimal("36000")
        );

        ExpenseParticipant expenseParticipant2 = ExpenseParticipant.createExpenseParticipant(
                tripJoin2,
                new BigDecimal("18000")
        );

        Expense expense1 = SharedExpense.createSharedExpense(
                LocalDateTime.now(),
                "초밥",
                PaymentMethod.CARD,
                ExpenseCategory.FOOD,
                CurrencyType.KRW,
                new BigDecimal("1"),
                CalculateType.EACH,
                null,
                List.of(expenseParticipant1, expenseParticipant2),
                schedule
        );

        // 햄버거
        ExpenseParticipant expenseParticipant3 = ExpenseParticipant.createExpenseParticipant(
                tripJoin2,
                new BigDecimal("7000")
        );

        ExpenseParticipant expenseParticipant4 = ExpenseParticipant.createExpenseParticipant(
                tripJoin3,
                new BigDecimal("2000")
        );

        Expense expense2 = SharedExpense.createSharedExpense(
                LocalDateTime.now(),
                "햄버거",
                PaymentMethod.CARD,
                ExpenseCategory.FOOD,
                CurrencyType.KRW,
                new BigDecimal("1"),
                CalculateType.EACH,
                null,
                List.of(expenseParticipant3, expenseParticipant4),
                schedule
        );

        // 음료
        ExpenseParticipant expenseParticipant5 = ExpenseParticipant.createExpenseParticipant(
                tripJoin1,
                new BigDecimal("3000")
        );

        ExpenseParticipant expenseParticipant6 = ExpenseParticipant.createExpenseParticipant(
                tripJoin2,
                new BigDecimal("6000")
        );

        ExpenseParticipant expenseParticipant7 = ExpenseParticipant.createExpenseParticipant(
                tripJoin3,
                new BigDecimal("3000")
        );

        Expense expense3 = SharedExpense.createSharedExpense(
                LocalDateTime.now(),
                "음료",
                PaymentMethod.CARD,
                ExpenseCategory.FOOD,
                CurrencyType.KRW,
                new BigDecimal("1"),
                CalculateType.EACH,
                null,
                List.of(expenseParticipant5, expenseParticipant6, expenseParticipant7),
                schedule
        );

        // 빵
        ExpenseParticipant expenseParticipant8 = ExpenseParticipant.createExpenseParticipant(
                tripJoin1,
                new BigDecimal("6000")
        );

        ExpenseParticipant expenseParticipant9 = ExpenseParticipant.createExpenseParticipant(
                tripJoin2,
                new BigDecimal("3000")
        );

        Expense expense4 = SharedExpense.createSharedExpense(
                LocalDateTime.now(),
                "음료",
                PaymentMethod.CARD,
                ExpenseCategory.FOOD,
                CurrencyType.KRW,
                new BigDecimal("1"),
                CalculateType.EACH,
                tripJoin1,
                List.of(expenseParticipant8, expenseParticipant9),
                schedule
        );

        expenseRepository.saveAll(List.of(expense1, expense2, expense3, expense4));
        expenseParticipantRepository.saveAll(List.of(expenseParticipant1, expenseParticipant2, expenseParticipant3, expenseParticipant4, expenseParticipant5, expenseParticipant6, expenseParticipant7, expenseParticipant8, expenseParticipant9));
    }
}

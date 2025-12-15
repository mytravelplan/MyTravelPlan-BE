package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.budget.entity.BudgetParticipant;
import travel.mytravelplan.domain.budget.entity.SharedBudget;
import travel.mytravelplan.domain.budget.exception.BudgetException;
import travel.mytravelplan.domain.budget.repository.BudgetParticipantRepository;
import travel.mytravelplan.domain.budget.repository.BudgetRepository;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.PaymentMethod;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.global.error.code.BudgetErrorCode;
import travel.mytravelplan.global.error.code.TripErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Profile("local")
@Component
@Order(4)
@RequiredArgsConstructor
public class BudgetInitializer implements ApplicationRunner {
    private final BudgetRepository budgetRepository;
    private final BudgetParticipantRepository budgetParticipantRepository;
    private final TripRepository tripRepository;
    private final TripJoinRepository tripJoinRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Trip trip = tripRepository.findById(1L)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        TripJoin tripJoin1 = tripJoinRepository.findById(1L)
                .orElseThrow(() -> new BudgetException(BudgetErrorCode.BUDGET_PARTICIPANT_NOT_FOUND));

        TripJoin tripJoin2 = tripJoinRepository.findById(2L)
                .orElseThrow(() -> new BudgetException(BudgetErrorCode.BUDGET_PARTICIPANT_NOT_FOUND));

        TripJoin tripJoin3 = tripJoinRepository.findById(3L)
                .orElseThrow(() -> new BudgetException(BudgetErrorCode.BUDGET_PARTICIPANT_NOT_FOUND));

        BudgetParticipant budgetParticipant1 = BudgetParticipant.createBudgetParticipant(
                tripJoin1,
                new BigDecimal("50000")
        );

        BudgetParticipant budgetParticipant2 = BudgetParticipant.createBudgetParticipant(
                tripJoin2,
                new BigDecimal("15000")
        );

        BudgetParticipant budgetParticipant3 = BudgetParticipant.createBudgetParticipant(
                tripJoin2,
                new BigDecimal("15000")
        );

        BudgetParticipant budgetParticipant4 = BudgetParticipant.createBudgetParticipant(
                tripJoin3,
                new BigDecimal("10000")
        );

        SharedBudget sharedBudget1 = SharedBudget.createSharedBudget(
                LocalDateTime.now(),
                "예산 1",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                new BigDecimal("1"),
                CalculateType.EACH,
                List.of(budgetParticipant1, budgetParticipant2),
                trip
        );

        SharedBudget sharedBudget2 = SharedBudget.createSharedBudget(
                LocalDateTime.now(),
                "예산 2",
                PaymentMethod.CASH,
                CurrencyType.KRW,
                new BigDecimal("1"),
                CalculateType.EACH,
                List.of(budgetParticipant3, budgetParticipant4),
                trip
        );

        budgetRepository.saveAll(List.of(sharedBudget1, sharedBudget2));
        budgetParticipantRepository.saveAll(List.of(budgetParticipant1, budgetParticipant2, budgetParticipant3, budgetParticipant4));
    }
}

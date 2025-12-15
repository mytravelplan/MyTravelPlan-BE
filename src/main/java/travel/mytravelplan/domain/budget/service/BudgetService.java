package travel.mytravelplan.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.budget.dto.*;
import travel.mytravelplan.domain.budget.entity.Budget;
import travel.mytravelplan.domain.budget.entity.BudgetParticipant;
import travel.mytravelplan.domain.budget.entity.PersonalBudget;
import travel.mytravelplan.domain.budget.entity.SharedBudget;
import travel.mytravelplan.domain.budget.enums.BudgetType;
import travel.mytravelplan.domain.budget.exception.BudgetException;
import travel.mytravelplan.domain.budget.mapper.BudgetMapper;
import travel.mytravelplan.domain.budget.repository.BudgetParticipantRepository;
import travel.mytravelplan.domain.budget.repository.BudgetRepository;
import travel.mytravelplan.domain.currency.exception.CurrencyException;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.BudgetErrorCode;
import travel.mytravelplan.global.error.code.CurrencyErrorCode;
import travel.mytravelplan.global.error.code.TripErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetParticipantRepository budgetParticipantRepository;
    private final TripRepository tripRepository;
    private final TripJoinRepository tripJoinRepository;
    private final TripCurrencyRepository tripCurrencyRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    public BudgetDto createBudget(Long tripId, BudgetCreateRequestDto budgetCreateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        BudgetType budgetType = budgetCreateRequestDto.getBudgetType();

        if (budgetType == BudgetType.SHARED) {
            SharedBudgetCreateRequestDto sharedBudgetCreateRequestDto = (SharedBudgetCreateRequestDto) budgetCreateRequestDto;

            List<BudgetParticipant> budgetParticipants = new ArrayList<>();

            if (sharedBudgetCreateRequestDto.getCalculateType() == CalculateType.EQUAL) {
                List<TripJoin> tripJoins = resolveParticipants(sharedBudgetCreateRequestDto.getBudgetParticipants(), tripId);

                int n = tripJoins.size();

                BigDecimal equalAmount = sharedBudgetCreateRequestDto.getBudgetParticipants()
                        .stream()
                        .map(BudgetParticipantRequestDto::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(n), RoundingMode.HALF_UP);

                tripJoins.forEach(tripJoin -> {
                    BudgetParticipant budgetParticipant = BudgetParticipant.createBudgetParticipant(tripJoin, equalAmount);
                    budgetParticipants.add(budgetParticipant);
                });
            } else if (sharedBudgetCreateRequestDto.getCalculateType() == CalculateType.EACH) {
                List<BudgetParticipantRequestDto> budgetParticipantRequestDtos = sharedBudgetCreateRequestDto.getBudgetParticipants().stream()
                        .filter(participant -> participant.getAmount().compareTo(BigDecimal.ZERO) > 0)
                        .toList();

                List<TripJoin> tripJoins = resolveParticipants(sharedBudgetCreateRequestDto.getBudgetParticipants(), tripId);

                Map<Long, TripJoin> tripJoinMap = tripJoins.stream()
                        .collect(Collectors.toMap(tj -> tj.getUser().getId(), tj -> tj));

                budgetParticipantRequestDtos.forEach(budgetParticipantRequestDto -> {
                    TripJoin tripJoin = tripJoinMap.get(budgetParticipantRequestDto.getId());
                    BudgetParticipant budgetParticipant = BudgetParticipant.createBudgetParticipant(tripJoin, budgetParticipantRequestDto.getAmount());
                    budgetParticipants.add(budgetParticipant);
                });
            } else {
                throw new BudgetException(BudgetErrorCode.INVALID_CALCULATE_TYPE);
            }

            SharedBudget sharedBudget = SharedBudget.createSharedBudget(
                    sharedBudgetCreateRequestDto.getDateTime(),
                    sharedBudgetCreateRequestDto.getMemo(),
                    sharedBudgetCreateRequestDto.getPaymentMethod(),
                    sharedBudgetCreateRequestDto.getCurrencyType(),
                    sharedBudgetCreateRequestDto.getExchangeRate() != null ? sharedBudgetCreateRequestDto.getExchangeRate() : tripCurrencyRepository.findByTripAndCurrencyType(
                                    trip, sharedBudgetCreateRequestDto.getCurrencyType())
                            .orElseThrow(() -> new CurrencyException(CurrencyErrorCode.CURRENCY_NOT_FOUND)).getExchangeRate(),
                    sharedBudgetCreateRequestDto.getCalculateType(),
                    budgetParticipants,
                    trip
            );

            budgetRepository.save(sharedBudget);
            budgetParticipantRepository.saveAll(budgetParticipants);
            return budgetMapper.toDto(sharedBudget);

        } else if (budgetType == BudgetType.PERSONAL) {
            PersonalBudgetCreateRequestDto personalBudgetCreateRequestDto = (PersonalBudgetCreateRequestDto) budgetCreateRequestDto;

            PersonalBudget personalBudget = PersonalBudget.createPersonalBudget(
                    personalBudgetCreateRequestDto.getDateTime(),
                    personalBudgetCreateRequestDto.getMemo(),
                    personalBudgetCreateRequestDto.getPaymentMethod(),
                    personalBudgetCreateRequestDto.getCurrencyType(),
                    personalBudgetCreateRequestDto.getExchangeRate() != null ? personalBudgetCreateRequestDto.getExchangeRate() : tripCurrencyRepository.findByTripAndCurrencyType(
                                    trip, personalBudgetCreateRequestDto.getCurrencyType())
                            .orElseThrow(() -> new CurrencyException(CurrencyErrorCode.CURRENCY_NOT_FOUND)).getExchangeRate(),
                    personalBudgetCreateRequestDto.getTotalAmount(),
                    trip
            );

            budgetRepository.save(personalBudget);
            return budgetMapper.toDto(personalBudget);
        } else {
            throw new BudgetException(BudgetErrorCode.INVALID_BUDGET_TYPE);
        }
    }

    public BudgetDto getBudget(Long tripId, Long budgetId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetException(BudgetErrorCode.BUDGET_NOT_FOUND));

        validateBudgetBelongsToTrip(budget, trip);

        return budgetMapper.toDto(budget);
    }

    public CursorPageResponseDto<BudgetDto> getBudgets(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        List<Budget> budgets = budgetRepository.findAllByCursor(trip.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = budgets.size() > limit;

        List<Budget> pagedBudgets = hasNext ? budgets.subList(0, limit) : budgets;

        List<BudgetDto> budgetDtos = pagedBudgets.stream()
                .map(budgetMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Budget lastBudget = pagedBudgets.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastBudget.getCreatedAt().toString();
            }

            nextAfter = lastBudget.getId();
        }

        return CursorPageResponseDto.<BudgetDto>builder()
                .content(budgetDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(budgetDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public BudgetDto updateBudget(Long tripId, Long budgetId, BudgetUpdateRequestDto budgetUpdateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetException(BudgetErrorCode.BUDGET_NOT_FOUND));

        validateBudgetBelongsToTrip(budget, trip);

        if (budget instanceof SharedBudget sharedBudget) {
            List<BudgetParticipant> budgetParticipants = new ArrayList<>();

            SharedBudgetUpdateRequestDto sharedBudgetUpdateRequestDto = (SharedBudgetUpdateRequestDto) budgetUpdateRequestDto;

            if (sharedBudgetUpdateRequestDto.getCalculateType() == CalculateType.EQUAL) {
                List<TripJoin> tripJoins = resolveParticipants(sharedBudgetUpdateRequestDto.getBudgetParticipants(), tripId);

                int n = tripJoins.size();

                BigDecimal equalAmount = sharedBudgetUpdateRequestDto.getBudgetParticipants().stream()
                        .map(BudgetParticipantRequestDto::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(n), RoundingMode.HALF_UP);

                tripJoins.forEach(tripJoin -> {
                    BudgetParticipant budgetParticipant = BudgetParticipant.createBudgetParticipant(tripJoin, equalAmount);
                    budgetParticipants.add(budgetParticipant);
                });
            } else if (sharedBudgetUpdateRequestDto.getCalculateType() == CalculateType.EACH) {
                List<BudgetParticipantRequestDto> budgetParticipantRequestDtos = sharedBudgetUpdateRequestDto.getBudgetParticipants().stream()
                        .filter(budgetParticipantRequestDto -> budgetParticipantRequestDto.getAmount().compareTo(BigDecimal.ZERO) > 0)
                        .toList();

                List<TripJoin> tripJoins = resolveParticipants(sharedBudgetUpdateRequestDto.getBudgetParticipants(), tripId);

                Map<Long, TripJoin> tripJoinMap = tripJoins.stream()
                        .collect(Collectors.toMap(tj -> tj.getUser().getId(), tj -> tj));

                budgetParticipantRequestDtos.forEach(budgetParticipantRequestDto -> {
                    TripJoin tripJoin = tripJoinMap.get(budgetParticipantRequestDto.getId());
                    BudgetParticipant budgetParticipant = BudgetParticipant.createBudgetParticipant(tripJoin, budgetParticipantRequestDto.getAmount());
                    budgetParticipants.add(budgetParticipant);
                });
            }

            sharedBudget.update(
                    sharedBudgetUpdateRequestDto.getDateTime(),
                    sharedBudgetUpdateRequestDto.getMemo(),
                    sharedBudgetUpdateRequestDto.getPaymentMethod(),
                    sharedBudgetUpdateRequestDto.getCurrencyType(),
                    budgetUpdateRequestDto.getExchangeRate() != null ? budgetUpdateRequestDto.getExchangeRate() : tripCurrencyRepository.findByTripAndCurrencyType(
                            trip, budgetUpdateRequestDto.getCurrencyType())
                            .orElseThrow(() -> new CurrencyException(CurrencyErrorCode.CURRENCY_NOT_FOUND)).getExchangeRate(),
                    sharedBudgetUpdateRequestDto.getCalculateType(),
                    budgetParticipants
            );

        } else if (budget instanceof PersonalBudget personalBudget) {
            PersonalBudgetUpdateRequestDto personalBudgetUpdateRequestDto = (PersonalBudgetUpdateRequestDto) budgetUpdateRequestDto;

            personalBudget.update(
                personalBudgetUpdateRequestDto.getDateTime(),
                personalBudgetUpdateRequestDto.getMemo(),
                personalBudgetUpdateRequestDto.getPaymentMethod(),
                personalBudgetUpdateRequestDto.getCurrencyType(),
                    budgetUpdateRequestDto.getExchangeRate() != null ? budgetUpdateRequestDto.getExchangeRate() : tripCurrencyRepository.findByTripAndCurrencyType(
                                    trip, budgetUpdateRequestDto.getCurrencyType())
                            .orElseThrow(() -> new CurrencyException(CurrencyErrorCode.CURRENCY_NOT_FOUND)).getExchangeRate(),
                personalBudgetUpdateRequestDto.getTotalAmount()
            );
        }

        return budgetMapper.toDto(budget);
    }

    @Transactional
    public void deleteBudget(Long tripId, Long budgetId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetException(BudgetErrorCode.BUDGET_NOT_FOUND));

        validateBudgetBelongsToTrip(budget, trip);

        budgetRepository.delete(budget);
    }

    private List<TripJoin> resolveParticipants(List<BudgetParticipantRequestDto> budgetParticipantRequestDtos, Long tripId) {
        long distinctCount = budgetParticipantRequestDtos.stream()
                .map(BudgetParticipantRequestDto::getId)
                .distinct()
                .count();

        if (distinctCount != budgetParticipantRequestDtos.size()) {
            throw new BudgetException(BudgetErrorCode.DUPLICATE_BUDGET_PARTICIPANTS);
        }

        List<Long> ids = budgetParticipantRequestDtos.stream()
                .map(BudgetParticipantRequestDto::getId)
                .toList();

        List<TripJoin> joins = tripJoinRepository.findByTripIdAndUserIdIn(tripId, ids);

        if (joins.size() != ids.size()) {
            throw new BudgetException(BudgetErrorCode.BUDGET_PARTICIPANT_NOT_FOUND);
        }

        return joins;
    }

    private void validateBudgetBelongsToTrip(Budget budget, Trip trip) {
        if (!budget.getTrip().equals(trip)) {
            throw new BudgetException(BudgetErrorCode.BUDGET_NOT_BELONG_TO_TRIP);
        }
    }
}

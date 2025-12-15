package travel.mytravelplan.domain.expense.service;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.budget.entity.BudgetParticipant;
import travel.mytravelplan.domain.budget.dto.BudgetSettleDto;
import travel.mytravelplan.domain.budget.entity.SharedBudget;
import travel.mytravelplan.domain.budget.repository.BudgetRepository;
import travel.mytravelplan.domain.currency.exception.TripCurrencyException;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.expense.dto.*;
import travel.mytravelplan.domain.expense.entity.Expense;
import travel.mytravelplan.domain.expense.entity.ExpenseParticipant;
import travel.mytravelplan.domain.expense.entity.PersonalExpense;
import travel.mytravelplan.domain.expense.entity.SharedExpense;
import travel.mytravelplan.domain.expense.enums.CalculateType;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.exception.ExpenseException;
import travel.mytravelplan.domain.expense.repository.ExpenseParticipantRepository;
import travel.mytravelplan.domain.expense.repository.ExpenseRepository;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.schedule.exception.ScheduleException;
import travel.mytravelplan.domain.expense.mapper.ExpenseMapper;
import travel.mytravelplan.domain.schedule.repository.ScheduleRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.expense.enums.GroupByType;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;
    private final ExpenseMapper expenseMapper;
    private final TripJoinRepository tripJoinRepository;
    private final ScheduleRepository scheduleRepository;
    private final TripCurrencyRepository tripCurrencyRepository;
    private final BudgetRepository budgetRepository;
    private final TripRepository tripRepository;

    @Transactional
    public ExpenseDto createExpense(Long tripId, Long scheduleId, ExpenseCreateRequestDto expenseCreateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleBelongsToTrip(schedule, trip);

        ExpenseType expenseType = expenseCreateRequestDto.getExpenseType();

        if (expenseType == ExpenseType.SHARED) {
            SharedExpenseCreateRequestDto sharedExpenseCreateRequestDto = (SharedExpenseCreateRequestDto) expenseCreateRequestDto;
            List<ExpenseParticipant> expenseParticipants = new ArrayList<>();

            TripJoin payer = tripJoinRepository.findByUserIdAndTripId(sharedExpenseCreateRequestDto.getPayerId(), tripId)
                    .orElseThrow(() -> new ExpenseException(ExpenseErrorCode.PAYER_NOT_FOUND));

            CalculateType calculateType = sharedExpenseCreateRequestDto.getCalculateType();

            if (calculateType == CalculateType.EQUAL) {
                List<TripJoin> tripJoins = resolveParticipants(sharedExpenseCreateRequestDto.getExpenseParticipants(), tripId);

                int n = tripJoins.size();

                BigDecimal equalAmount = sharedExpenseCreateRequestDto.getExpenseParticipants().stream()
                        .map(ExpenseParticipantRequestDto::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(n), RoundingMode.HALF_UP);

                tripJoins.forEach(tripJoin -> {
                    ExpenseParticipant expenseParticipant = ExpenseParticipant.createExpenseParticipant(tripJoin, equalAmount);
                    expenseParticipants.add(expenseParticipant);
                });
            }else if (calculateType == CalculateType.EACH) {
                List<ExpenseParticipantRequestDto> expenseParticipantRequestDtos = sharedExpenseCreateRequestDto.getExpenseParticipants().stream()
                        .filter(participant -> participant.getAmount().compareTo(BigDecimal.ZERO) > 0)
                        .toList();

                List<TripJoin> tripJoins = resolveParticipants(sharedExpenseCreateRequestDto.getExpenseParticipants(), tripId);

                Map<Long, TripJoin> tripJoinMap = tripJoins.stream()
                        .collect(Collectors.toMap(tj -> tj.getUser().getId(), tj -> tj));

                expenseParticipantRequestDtos.forEach(expenseParticipantRequestDto -> {
                    TripJoin tripJoin = tripJoinMap.get(expenseParticipantRequestDto.getId());
                    ExpenseParticipant expenseParticipant = ExpenseParticipant.createExpenseParticipant(tripJoin, expenseParticipantRequestDto.getAmount());
                    expenseParticipants.add(expenseParticipant);
                });
            } else {
                throw new ExpenseException(ExpenseErrorCode.INVALID_CALCULATE_TYPE);
            }

            SharedExpense sharedExpense = SharedExpense.createSharedExpense(
                    sharedExpenseCreateRequestDto.getDateTime(),
                    sharedExpenseCreateRequestDto.getMemo(),
                    sharedExpenseCreateRequestDto.getPaymentMethod(),
                    sharedExpenseCreateRequestDto.getCategory(),
                    sharedExpenseCreateRequestDto.getCurrencyType(),
                    sharedExpenseCreateRequestDto.getExchangeRate() != null ? sharedExpenseCreateRequestDto.getExchangeRate() : tripCurrencyRepository.findByTripAndCurrencyType(
                            trip, sharedExpenseCreateRequestDto.getCurrencyType())
                            .orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND)).getExchangeRate(),
                    sharedExpenseCreateRequestDto.getCalculateType(),
                    payer,
                    expenseParticipants,
                    schedule
            );

            expenseRepository.save(sharedExpense);
            expenseParticipantRepository.saveAll(expenseParticipants);

            return expenseMapper.toDto(sharedExpense);
        } else if (expenseType == ExpenseType.PERSONAL) {
            PersonalExpenseCreateRequestDto personalExpenseCreateRequestDto = (PersonalExpenseCreateRequestDto) expenseCreateRequestDto;

            PersonalExpense personalExpense = PersonalExpense.createPersonalExpense(
                    personalExpenseCreateRequestDto.getDateTime(),
                    personalExpenseCreateRequestDto.getMemo(),
                    personalExpenseCreateRequestDto.getPaymentMethod(),
                    personalExpenseCreateRequestDto.getCategory(),
                    personalExpenseCreateRequestDto.getCurrencyType(),
                    personalExpenseCreateRequestDto.getExchangeRate() != null ? personalExpenseCreateRequestDto.getExchangeRate() : tripCurrencyRepository.findByTripAndCurrencyType(
                                    trip, personalExpenseCreateRequestDto.getCurrencyType())
                            .orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND)).getExchangeRate(),
                    personalExpenseCreateRequestDto.getTotalAmount(),
                    schedule
            );

            expenseRepository.save(personalExpense);
            return expenseMapper.toDto(personalExpense);
        } else {
            throw new ExpenseException(ExpenseErrorCode.INVALID_EXPENSE_TYPE);
        }
    }

    public ExpenseDto getExpense(Long tripId, Long scheduleId, Long expenseId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleBelongsToTrip(schedule, trip);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseException(ExpenseErrorCode.EXPENSE_NOT_FOUND));
        return expenseMapper.toDto(expense);
    }

    public CursorPageResponseDto<ExpenseDto> getExpenses(Long tripId, Long scheduleId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleBelongsToTrip(schedule, trip);

        List<Expense> expenses = expenseRepository.findAllByCursor(schedule.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = expenses.size() > limit;

        List<Expense> pagedExpenses = hasNext ? expenses.subList(0, limit) : expenses;

        List<ExpenseDto> expenseDtos = pagedExpenses.stream()
                .map(expenseMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Expense lastExpense = pagedExpenses.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastExpense.getCreatedAt().toString();
            }
            nextAfter = lastExpense.getId();
        }

        return CursorPageResponseDto.<ExpenseDto>builder()
                .content(expenseDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(expenseDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public ExpenseDto updateExpense(Long tripId, Long scheduleId, Long expenseId, ExpenseUpdateRequestDto expenseUpdateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleBelongsToTrip(schedule, trip);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseException(ExpenseErrorCode.EXPENSE_NOT_FOUND));

        if (expense instanceof SharedExpense sharedExpense) {
            SharedExpenseUpdateRequestDto sharedExpenseUpdateRequestDto = (SharedExpenseUpdateRequestDto) expenseUpdateRequestDto;

            List<ExpenseParticipant> expenseParticipants = new ArrayList<>();

            TripJoin payer = tripJoinRepository.findByUserIdAndTripId(sharedExpenseUpdateRequestDto.getPayerId(), tripId)
                    .orElseThrow(() -> new ExpenseException(ExpenseErrorCode.PAYER_NOT_FOUND));

            CalculateType calculateType = sharedExpenseUpdateRequestDto.getCalculateType();

            if (calculateType == CalculateType.EQUAL) {
                List<TripJoin> tripJoins = resolveParticipants(sharedExpenseUpdateRequestDto.getExpenseParticipants(), tripId);

                int n = tripJoins.size();

                BigDecimal equalAmount = sharedExpenseUpdateRequestDto.getExpenseParticipants().stream()
                        .map(ExpenseParticipantRequestDto::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(n), RoundingMode.HALF_UP);

                tripJoins.forEach(tripJoin -> {
                    ExpenseParticipant expenseParticipant = ExpenseParticipant.createExpenseParticipant(tripJoin, equalAmount);
                    expenseParticipants.add(expenseParticipant);
                });
            }else if (calculateType == CalculateType.EACH) {
                List<ExpenseParticipantRequestDto> expenseParticipantRequestDtos = sharedExpenseUpdateRequestDto.getExpenseParticipants().stream()
                        .filter(participant -> participant.getAmount().compareTo(BigDecimal.ZERO) > 0)
                        .toList();

                List<TripJoin> tripJoins = resolveParticipants(sharedExpenseUpdateRequestDto.getExpenseParticipants(), tripId);

                Map<Long, TripJoin> tripJoinMap = tripJoins.stream()
                        .collect(Collectors.toMap(tj -> tj.getUser().getId(), tj -> tj));

                expenseParticipantRequestDtos.forEach(expenseParticipantRequestDto -> {
                    TripJoin tripJoin = tripJoinMap.get(expenseParticipantRequestDto.getId());
                    ExpenseParticipant expenseParticipant = ExpenseParticipant.createExpenseParticipant(tripJoin, expenseParticipantRequestDto.getAmount());
                    expenseParticipants.add(expenseParticipant);
                });
            } else {
                throw new ExpenseException(ExpenseErrorCode.INVALID_CALCULATE_TYPE);
            }

            sharedExpense.update(
                    sharedExpenseUpdateRequestDto.getDateTime(),
                    sharedExpenseUpdateRequestDto.getMemo(),
                    sharedExpenseUpdateRequestDto.getPaymentMethod(),
                    sharedExpenseUpdateRequestDto.getCategory(),
                    sharedExpenseUpdateRequestDto.getCurrencyType(),
                    sharedExpenseUpdateRequestDto.getExchangeRate() != null ? sharedExpenseUpdateRequestDto.getExchangeRate() : tripCurrencyRepository.findByTripAndCurrencyType(
                                    trip, sharedExpenseUpdateRequestDto.getCurrencyType())
                            .orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND)).getExchangeRate(),
                    sharedExpenseUpdateRequestDto.getCalculateType(),
                    payer,
                    expenseParticipants
            );
        }else if (expense instanceof PersonalExpense personalExpense) {
            PersonalExpenseUpdateRequestDto personalExpenseUpdateRequestDto = (PersonalExpenseUpdateRequestDto) expenseUpdateRequestDto;

            personalExpense.update(
                    personalExpenseUpdateRequestDto.getDateTime(),
                    personalExpenseUpdateRequestDto.getMemo(),
                    personalExpenseUpdateRequestDto.getPaymentMethod(),
                    personalExpenseUpdateRequestDto.getCategory(),
                    personalExpenseUpdateRequestDto.getCurrencyType(),
                    personalExpenseUpdateRequestDto.getExchangeRate() != null ? personalExpenseUpdateRequestDto.getExchangeRate() : tripCurrencyRepository.findByTripAndCurrencyType(
                                    trip, personalExpenseUpdateRequestDto.getCurrencyType())
                            .orElseThrow(() -> new TripCurrencyException(TripCurrencyErrorCode.TRIP_CURRENCY_NOT_FOUND)).getExchangeRate(),
                    personalExpenseUpdateRequestDto.getTotalAmount()
            );
        }else {
            throw new ExpenseException(ExpenseErrorCode.INVALID_EXPENSE_TYPE);
        }

        return expenseMapper.toDto(expense);
    }

    @Transactional
    public void deleteExpense(Long tripId, Long scheduleId, Long expenseId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleBelongsToTrip(schedule, trip);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseException(ExpenseErrorCode.EXPENSE_NOT_FOUND));

        expenseRepository.delete(expense);
    }

    public SettleExpenseDto settleExpenses(Long tripId) {
        // 기본 데이터 조회
        List<SharedExpense> sharedExpenses = expenseRepository.findSharedExpenseAllByTripId(tripId);
        List<SharedBudget> sharedBudgets = budgetRepository.findSharedBudgetExpenseAllByTripId(tripId);

        // 1. 예산 기반 사용자별 초기 납입금 계산
        Map<Long, UserExpenseDto> contributionByJoin = sharedBudgets.stream()
                .flatMap(sb -> sb.getBudgetParticipants().stream())
                .collect(Collectors.toMap(
                        bp -> bp.getTripJoin().getId(),
                        bp -> UserExpenseDto.builder()
                                .tripJoinId(bp.getTripJoin().getId())
                                .paidAmount(bp.getAmount())
                                .consumedAmount(BigDecimal.ZERO)
                                .build(),
                        (existing, newDto) -> UserExpenseDto.builder()
                                .tripJoinId(existing.getTripJoinId())
                                .paidAmount(existing.getPaidAmount().add(newDto.getPaidAmount()))
                                .consumedAmount(existing.getConsumedAmount())
                                .build()));

        // 사용자별 순 잔액 계산용 맵 초기화
        Map<Long, BigDecimal> netBalance = new HashMap<>();

        // 2. 전체 예산 및 지출 금액 계산
        BigDecimal totalCollected = sharedBudgets.stream()
                .flatMap(sb -> sb.getBudgetParticipants().stream())
                .map(BudgetParticipant::getAmount)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal totalPaid = sharedExpenses.stream()
                .filter(e -> e.getPayer() == null)
                .map(e -> e.getExpenseParticipants().stream()
                        .map(ExpenseParticipant::getAmount)
                        .reduce(ZERO, BigDecimal::add))
                .reduce(ZERO, BigDecimal::add);

        BigDecimal remainingBudget = totalCollected.subtract(totalPaid);

        Map<Long, UserExpenseDto> userExpenseMap = new HashMap<>();

        // 3. 남은 예산 환급 처리 (납입 비율에 따라 분배)
        for (Map.Entry<Long, UserExpenseDto> entry : contributionByJoin.entrySet()) {
            Long tripJoinId = entry.getKey();
            BigDecimal userPaidAmount = entry.getValue().getPaidAmount();

            // 사용자 납입 비율에 따른 환급액 계산
            BigDecimal refundAmount = remainingBudget
                    .multiply(userPaidAmount)
                    .divide(totalCollected, RoundingMode.HALF_UP);

            // 납입금에서 환급액 차감
            netBalance.put(tripJoinId, userPaidAmount.subtract(refundAmount));
        }

        // 4. 개별 지출 내역 처리
        for (SharedExpense sharedExpense : sharedExpenses) {
            for (ExpenseParticipant expenseParticipant : sharedExpense.getExpenseParticipants()) {
                Long tripJoinId = expenseParticipant.getTripJoin().getId();
                Long userId = expenseParticipant.getTripJoin().getUser().getId();
                String username = expenseParticipant.getTripJoin().getUser().getUsername();
                String nickname = expenseParticipant.getTripJoin().getUser().getUserProfile().getNickname();
                String profileImageUrl = expenseParticipant.getTripJoin().getUser().getUserProfile().getProfileImageUrl();

                // 기존 사용자 정보 가져오기 (없으면 기본값 생성)
                UserExpenseDto currentUserData = userExpenseMap.getOrDefault(tripJoinId,
                        UserExpenseDto.builder()
                                .userId(tripJoinId)
                                .tripJoinId(tripJoinId)
                                .nickname(nickname)
                                .username(username)
                                .profileImageUrl(profileImageUrl)
                                .paidAmount(ZERO)
                                .consumedAmount(ZERO)
                                .build());

                BigDecimal paidDelta;

                // 지불 금액 계산
                if (sharedExpense.getPayer() != null && sharedExpense.getPayer().getId().equals(tripJoinId)) {
                    paidDelta = sharedExpense.getExpenseParticipants().stream()
                            .map(ExpenseParticipant::getAmount)
                            .reduce(ZERO, BigDecimal::add);
                } else {
                    paidDelta = ZERO;
                }

                // 소비 금액
                BigDecimal consumedDelta = expenseParticipant.getAmount();

                // 사용자 데이터 업데이트
                userExpenseMap.put(tripJoinId, UserExpenseDto.builder()
                        .tripJoinId(tripJoinId)
                        .userId(userId)
                        .nickname(nickname)
                        .username(username)
                        .profileImageUrl(profileImageUrl)
                        .paidAmount(currentUserData.getPaidAmount().add(paidDelta))
                        .consumedAmount(currentUserData.getConsumedAmount().add(consumedDelta))
                        .build());

                // 순 잔액 업데이트 (지불액 - 소비액)
                netBalance.merge(tripJoinId, paidDelta.subtract(consumedDelta), BigDecimal::add);
            }
        }

        // 5. 결과 생성
        BudgetSettleDto budgetSummary = BudgetSettleDto.builder()
                .totalCollectedAmount(totalCollected)
                .totalPaidAmount(totalPaid)
                .remainingAmount(remainingBudget)
                .build();

        List<TransferDto> transferList = matchTransfers(netBalance, userExpenseMap);

        return SettleExpenseDto.builder()
                .budget(budgetSummary)
                .transferList(transferList)
                .expenseList(new ArrayList<>(userExpenseMap.values()))
                .build();
    }

    public ExpenseStatisticsDto getExpenseStatistics(Long tripId, ExpenseType expenseType, GroupByType groupBy, LocalDate date) {
        return expenseRepository.getExpenseStatistics(tripId, expenseType, groupBy, date);
    }

    private List<TripJoin> resolveParticipants(List<ExpenseParticipantRequestDto> requests, Long tripId) {
        // 중복 ID 체크
        long distinctCount = requests.stream()
                .map(ExpenseParticipantRequestDto::getId)
                .distinct()
                .count();
        if (distinctCount != requests.size()) {
            throw new ExpenseException(ExpenseErrorCode.DUPLICATE_EXPENSE_PARTICIPANTS);
        }

        List<Long> ids = requests.stream()
                .map(ExpenseParticipantRequestDto::getId)
                .collect(Collectors.toList());

        List<TripJoin> joins = tripJoinRepository.findByTripIdAndUserIdIn(tripId, ids);
        if (joins.size() != ids.size()) {
            throw new ExpenseException(ExpenseErrorCode.EXPENSE_PARTICIPANT_NOT_FOUND);
        }
        return joins;
    }

    private List<TransferDto> matchTransfers(Map<Long, BigDecimal> netBalance, Map<Long, UserExpenseDto> userExpenseMap) {
        PriorityQueue<Map.Entry<Long, BigDecimal>> creditors = new PriorityQueue<>(
                (a, b) -> b.getValue().compareTo(a.getValue())
        );
        PriorityQueue<Map.Entry<Long, BigDecimal>> debtors = new PriorityQueue<>(
                (a, b) -> b.getValue().compareTo(a.getValue())
        );

        for (Map.Entry<Long, BigDecimal> e : netBalance.entrySet()) {
            int cmp = e.getValue().compareTo(ZERO);
            if (cmp > 0) creditors.offer(Map.entry(e.getKey(), e.getValue()));
            else if (cmp < 0) debtors.offer(Map.entry(e.getKey(), e.getValue().abs()));
        }

        List<TransferDto> transferList = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<Long, BigDecimal> c = creditors.poll();
            Map.Entry<Long, BigDecimal> d = debtors.poll();

            BigDecimal send = c.getValue().min(d.getValue());
            if (send.compareTo(ZERO) > 0) {
                UserExpenseDto fromDto = userExpenseMap.get(d.getKey()); // 채무자 정보
                UserExpenseDto toDto = userExpenseMap.get(c.getKey());   // 채권자 정보

                TransferDto.UserDto fromUser = TransferDto.UserDto.builder()
                        .userId(fromDto.getUserId())
                        .username(fromDto.getUsername())
                        .nickname(fromDto.getNickname())
                        .profileImageUrl(fromDto.getProfileImageUrl())
                        .build();

                TransferDto.UserDto toUser = TransferDto.UserDto.builder()
                        .userId(toDto.getUserId())
                        .username(toDto.getUsername())
                        .nickname(toDto.getNickname())
                        .profileImageUrl(toDto.getProfileImageUrl())
                        .build();

                transferList.add(TransferDto.builder()
                        .from(fromUser)
                        .to(toUser)
                        .amount(send)
                        .build());
            }

            BigDecimal cLeft = c.getValue().subtract(send);
            BigDecimal dLeft = d.getValue().subtract(send);

            if (cLeft.compareTo(ZERO) > 0) creditors.offer(Map.entry(c.getKey(), cLeft));
            if (dLeft.compareTo(ZERO) > 0) debtors.offer(Map.entry(d.getKey(), dLeft));
        }
        return transferList;
    }

    public ByteArrayResource exportExpensesToExcel(Long tripId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Expense> expenses = expenseRepository.findAllByTripId(tripId);

        try (Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            CSVWriter csv = new CSVWriter(w)) {

            String[] header = {"날짜", "카테고리", "결제 수단", "금액", "통화", "통화 당 원화", "원화 환산", "결제한 사람", "정산할 사람", "메모"};

            csv.writeNext(header);

            for(Expense e : expenses) {
                String date = e.getDateTime().toLocalDate().toString();
                String category = e.getCategory().toString();
                String paymentMethod = e.getPaymentMethod().toString();
                String amount = "";
                String currency = e.getCurrencyType().toString();
                String exchangeRate = e.getExchangeRate().toString();
                String amountInKRW = "";
                String payer = "";
                String participants = "";

                if (e instanceof PersonalExpense) {
                    PersonalExpense personalExpense = (PersonalExpense) e;

                    amount = personalExpense.getTotalAmount().toString();

                    amountInKRW = personalExpense.getTotalAmount().multiply(e.getExchangeRate()).toString();

                } else if(e instanceof SharedExpense) {
                    SharedExpense sharedExpense = (SharedExpense) e;

                    amount = sharedExpense.getExpenseParticipants().stream()
                            .map(ExpenseParticipant::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .toString();

                    amountInKRW = sharedExpense.getExpenseParticipants().stream()
                            .map(ExpenseParticipant::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .multiply(e.getExchangeRate())
                            .toString();

                    payer = sharedExpense.getPayer() != null ? sharedExpense.getPayer().getUser().getUserProfile().getNickname() : "공동경비";

                    participants = sharedExpense.getExpenseParticipants().stream()
                            .map(ep -> ep.getTripJoin().getUser().getUserProfile().getNickname() + " (" + ep.getAmount() + ")")
                            .collect(Collectors.joining(", "));
                }

                String memo = e.getMemo();

                String[] row = {date, category, paymentMethod, amount, currency, exchangeRate, amountInKRW, payer, participants, memo};
                csv.writeNext(row);
            }

            csv.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayResource(out.toByteArray());
    }

    private void validateScheduleBelongsToTrip(Schedule schedule, Trip trip) {
        if (!schedule.getTrip().getId().equals(trip.getId())) {
            throw new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_BELONG_TO_TRIP);
        }
    }
}
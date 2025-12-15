package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.schedule.repository.ScheduleRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.global.error.code.TripErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Profile("local")
@Component
@Order(3)
@RequiredArgsConstructor
public class ScheduleInitializer implements ApplicationRunner {
    private final TripRepository tripRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Trip trip = tripRepository.findById(1L)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        LocalDate day1 = trip.getStartDate();
        LocalDate day2 = day1.plusDays(1);
        LocalDate day3 = day2.plusDays(1);

        // 1일차
        Schedule schedule1 = Schedule.createSchedule(
                "도쿄 도착 & 호텔 체크인",
                LocalDateTime.of(day1, LocalTime.of(14, 0)),
                LocalDateTime.of(day1, LocalTime.of(16, 0)),
                "하네다/나리타 도착 후 체크인",
                scheduleRepository.findMaxDisplayOrderByTripId(trip.getId()) + 1,
                null,
                trip,
                new BigDecimal("5.0")
        );
        scheduleRepository.save(schedule1);

        Schedule schedule2 = Schedule.createSchedule(
                "시부야 스크램블 & 하치코",
                LocalDateTime.of(day1, LocalTime.of(18, 0)),
                LocalDateTime.of(day1, LocalTime.of(20, 0)),
                "시부야 산책 및 저녁 식사",
                scheduleRepository.findMaxDisplayOrderByTripId(trip.getId()) + 1,
                null,
                trip,
                new BigDecimal("5.0")
        );
        scheduleRepository.save(schedule2);

        // 2일차
        Schedule schedule3 = Schedule.createSchedule(
                "아사쿠사 센소지 & 나카미세",
                LocalDateTime.of(day2, LocalTime.of(9, 0)),
                LocalDateTime.of(day2, LocalTime.of(12, 0)),
                "센소지 참배, 스카이트리 뷰",
                scheduleRepository.findMaxDisplayOrderByTripId(trip.getId()) + 1,
                null,
                trip,
                new BigDecimal("5.0")
        );
        scheduleRepository.save(schedule3);

        Schedule schedule4 = Schedule.createSchedule(
                "우에노 공원 & 아메요코",
                LocalDateTime.of(day2, LocalTime.of(14, 0)),
                LocalDateTime.of(day2, LocalTime.of(18, 0)),
                "박물관/시장 구경",
                scheduleRepository.findMaxDisplayOrderByTripId(trip.getId()) + 1,
                null,
                trip,
                new BigDecimal("5.0")
        );
        scheduleRepository.save(schedule4);

        // 3일차
        Schedule schedule5 = Schedule.createSchedule(
                "하코네 당일치기",
                LocalDateTime.of(day3, LocalTime.of(8, 0)),
                LocalDateTime.of(day3, LocalTime.of(19, 0)),
                "오와쿠다니, 아시노코, 온천",
                scheduleRepository.findMaxDisplayOrderByTripId(trip.getId()) + 1,
                null,
                trip,
                new BigDecimal("5.0")
        );
        scheduleRepository.save(schedule5);

        Schedule schedule6 = Schedule.createSchedule(
                "신주쿠 야경 & 전망대",
                LocalDateTime.of(day3, LocalTime.of(20, 0)),
                LocalDateTime.of(day3, LocalTime.of(22, 0)),
                "도쿄도청 전망대 또는 쇼핑",
                scheduleRepository.findMaxDisplayOrderByTripId(trip.getId()) + 1,
                null,
                trip,
                new BigDecimal("5.0")
        );
        scheduleRepository.save(schedule6);
    }
}

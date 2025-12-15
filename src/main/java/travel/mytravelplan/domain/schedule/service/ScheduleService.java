package travel.mytravelplan.domain.schedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.place.entity.Place;
import travel.mytravelplan.domain.place.exception.PlaceException;
import travel.mytravelplan.domain.place.repository.PlaceRepository;
import travel.mytravelplan.domain.schedule.dto.ScheduleCreateRequestDto;
import travel.mytravelplan.domain.schedule.dto.ScheduleDto;
import travel.mytravelplan.domain.schedule.dto.ScheduleUpdateRequestDto;
import travel.mytravelplan.domain.schedule.entity.Schedule;
import travel.mytravelplan.domain.schedule.exception.ScheduleException;
import travel.mytravelplan.domain.schedule.mapper.ScheduleMapper;
import travel.mytravelplan.domain.schedule.repository.ScheduleRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.PlaceErrorCode;
import travel.mytravelplan.global.error.code.ScheduleErrorCode;
import travel.mytravelplan.global.error.code.TripErrorCode;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final TripRepository tripRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleMapper scheduleMapper;

    @Transactional
    public ScheduleDto createSchedule(Long tripId, ScheduleCreateRequestDto scheduleCreateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        if (scheduleCreateRequestDto.getStartDateTime().toLocalDate().isBefore(trip.getStartDate()) ||
                scheduleCreateRequestDto.getEndDateTime().toLocalDate().isAfter(trip.getEndDate())) {
            throw new ScheduleException(ScheduleErrorCode.SCHEDULE_DATE_TIME_OUT_OF_TRIP_RANGE);
        }

        Place place = null;

        if (scheduleCreateRequestDto.getPlaceId() != null) {
            place = placeRepository.findById(scheduleCreateRequestDto.getPlaceId())
                    .orElseThrow(() -> new PlaceException(PlaceErrorCode.PLACE_NOT_FOUND));
        }

        Schedule schedule = Schedule.createSchedule(
                scheduleCreateRequestDto.getTitle(),
                scheduleCreateRequestDto.getStartDateTime(),
                scheduleCreateRequestDto.getEndDateTime(),
                scheduleCreateRequestDto.getMemo(),
                scheduleRepository.findMaxDisplayOrderByTripId(tripId) + 1,
                place,
                trip,
                scheduleCreateRequestDto.getRating()
        );

        scheduleRepository.save(schedule);

        return scheduleMapper.toDto(schedule);
    }

    public CursorPageResponseDto<ScheduleDto> getSchedules(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Schedule> schedules = scheduleRepository.findAllByCursor(tripId, keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = schedules.size() > limit;

        List<Schedule> pagedSchedules = hasNext ? schedules.subList(0, limit) : schedules;

        List<ScheduleDto> scheduleDtos = pagedSchedules.stream()
                .map(scheduleMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Schedule lastSchedule = pagedSchedules.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastSchedule.getCreatedAt().toString();
            }

            nextAfter = lastSchedule.getId();
        }

        return CursorPageResponseDto.<ScheduleDto>builder()
                .content(scheduleDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(scheduleDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public ScheduleDto getSchedule(Long tripId, Long scheduleId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleBelongsToTrip(schedule, trip);

        return scheduleMapper.toDto(schedule);
    }

    @Transactional
    public ScheduleDto updateSchedule(Long tripId, Long scheduleId, ScheduleUpdateRequestDto scheduleUpdateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleBelongsToTrip(schedule, trip);

        Place place = null;

        if (scheduleUpdateRequestDto.getPlaceId() != null) {
            place = placeRepository.findById(scheduleUpdateRequestDto.getPlaceId())
                    .orElseThrow(() -> new PlaceException(PlaceErrorCode.PLACE_NOT_FOUND));
        }

        schedule.update(
                scheduleUpdateRequestDto.getTitle(),
                scheduleUpdateRequestDto.getStartDateTime(),
                scheduleUpdateRequestDto.getEndDateTime(),
                scheduleUpdateRequestDto.getMemo(),
                scheduleUpdateRequestDto.getRating(),
                place
        );

        return scheduleMapper.toDto(schedule);
    }

    @Transactional
    public void deleteSchedule(Long tripId, Long scheduleId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleBelongsToTrip(schedule, trip);

        scheduleRepository.delete(schedule);
    }

    private void validateScheduleBelongsToTrip(Schedule schedule, Trip trip) {
        if (!schedule.getTrip().equals(trip)) {
            throw new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_BELONG_TO_TRIP);
        }
    }

}

package travel.mytravelplan.domain.schedule.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.place.entity.CustomPlace;
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
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@DisplayName("일정 서비스 테스트")
class ScheduleServiceTest extends ServiceTestSupport {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleService scheduleService;

    private Trip trip;
    private Place place;
    private Schedule schedule;
    private ScheduleDto scheduleDto;
    private ScheduleCreateRequestDto createRequestDto;
    private ScheduleUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 10);
        trip = Trip.createTrip("여행 제목", startDate, endDate, null, null);
        ReflectionTestUtils.setField(trip, "id", 1L);

        place = CustomPlace.createCustomPlace("장소명", "주소", null, null, null, null, null);
        ReflectionTestUtils.setField(place, "id", 1L);

        LocalDateTime scheduleStart = LocalDateTime.of(2025, 12, 2, 10, 0);
        LocalDateTime scheduleEnd = LocalDateTime.of(2025, 12, 2, 12, 0);

        schedule = Schedule.createSchedule(
                "일정 제목",
                scheduleStart,
                scheduleEnd,
                "메모",
                1L,
                place,
                trip,
                BigDecimal.valueOf(4.5)
        );
        ReflectionTestUtils.setField(schedule, "id", 1L);
        ReflectionTestUtils.setField(schedule, "createdAt", LocalDateTime.of(2025, 12, 1, 9, 0));

        scheduleDto = ScheduleDto.builder()
                .id(1L)
                .tripId(1L)
                .title("일정 제목")
                .memo("메모")
                .startDateTime(scheduleStart)
                .endDateTime(scheduleEnd)
                .placeName("장소명")
                .rating(BigDecimal.valueOf(4.5))
                .build();

        createRequestDto = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .startDateTime(scheduleStart)
                .endDateTime(scheduleEnd)
                .memo("메모")
                .rating(BigDecimal.valueOf(4.5))
                .placeId(1L)
                .build();

        updateRequestDto = ScheduleUpdateRequestDto.builder()
                .title("수정된 일정 제목")
                .startDateTime(scheduleStart)
                .endDateTime(scheduleEnd)
                .memo("수정된 메모")
                .rating(BigDecimal.valueOf(5.0))
                .placeId(1L)
                .build();
    }

    @Test
    @DisplayName("일정 생성 성공")
    void createSchedule_Success() {
        // given
        Long tripId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(placeRepository.findById(eq(1L))).willReturn(Optional.of(place));
        given(scheduleRepository.findMaxDisplayOrderByTripId(eq(tripId))).willReturn(0L);
        given(scheduleRepository.save(any(Schedule.class))).willReturn(schedule);
        lenient().when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

        // when
        ScheduleDto result = scheduleService.createSchedule(tripId, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(scheduleDto);

        then(tripRepository).should().findById(eq(tripId));
        then(placeRepository).should().findById(eq(1L));
        then(scheduleRepository).should().findMaxDisplayOrderByTripId(eq(tripId));
        then(scheduleRepository).should().save(any(Schedule.class));
        then(scheduleMapper).should().toDto(any(Schedule.class));
    }

    @Test
    @DisplayName("일정 생성 성공 - 장소 없이")
    void createSchedule_Success_WithoutPlace() {
        // given
        Long tripId = 1L;
        ScheduleCreateRequestDto requestWithoutPlace = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .startDateTime(LocalDateTime.of(2025, 12, 2, 10, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 2, 12, 0))
                .memo("메모")
                .rating(BigDecimal.valueOf(4.5))
                .placeId(null)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findMaxDisplayOrderByTripId(eq(tripId))).willReturn(0L);
        given(scheduleRepository.save(any(Schedule.class))).willReturn(schedule);
        lenient().when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

        // when
        ScheduleDto result = scheduleService.createSchedule(tripId, requestWithoutPlace);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(scheduleDto);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findMaxDisplayOrderByTripId(eq(tripId));
        then(scheduleRepository).should().save(any(Schedule.class));
        then(scheduleMapper).should().toDto(any(Schedule.class));
    }

    @Test
    @DisplayName("일정 생성 실패 - 여행을 찾을 수 없음")
    void createSchedule_TripNotFound() {
        // given
        Long tripId = 999L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.createSchedule(tripId, createRequestDto))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("일정 생성 실패 - 일정 날짜가 여행 기간을 벗어남 (시작일 이전)")
    void createSchedule_DateOutOfRange_BeforeStart() {
        // given
        Long tripId = 1L;
        ScheduleCreateRequestDto invalidRequest = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .startDateTime(LocalDateTime.of(2025, 11, 30, 10, 0))
                .endDateTime(LocalDateTime.of(2025, 11, 30, 12, 0))
                .memo("메모")
                .rating(BigDecimal.valueOf(4.5))
                .placeId(1L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> scheduleService.createSchedule(tripId, invalidRequest))
                .isInstanceOf(ScheduleException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("일정 생성 실패 - 일정 날짜가 여행 기간을 벗어남 (종료일 이후)")
    void createSchedule_DateOutOfRange_AfterEnd() {
        // given
        Long tripId = 1L;
        ScheduleCreateRequestDto invalidRequest = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .startDateTime(LocalDateTime.of(2025, 12, 11, 10, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 11, 12, 0))
                .memo("메모")
                .rating(BigDecimal.valueOf(4.5))
                .placeId(1L)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> scheduleService.createSchedule(tripId, invalidRequest))
                .isInstanceOf(ScheduleException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("일정 생성 실패 - 장소를 찾을 수 없음")
    void createSchedule_PlaceNotFound() {
        // given
        Long tripId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(placeRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.createSchedule(tripId, createRequestDto))
                .isInstanceOf(PlaceException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(placeRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("일정 목록 조회 성공")
    void getSchedules_Success() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Schedule> schedules = List.of(schedule);
        given(scheduleRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(schedules);
        lenient().when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

        // when
        CursorPageResponseDto<ScheduleDto> result = scheduleService.getSchedules(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(scheduleRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("일정 목록 조회 성공 - 다음 페이지 있음")
    void getSchedules_Success_WithNext() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        // 11개의 스케줄을 생성하여 hasNext가 true가 되도록 함
        List<Schedule> schedules = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Schedule tempSchedule = Schedule.createSchedule(
                    "일정 제목 " + i,
                    LocalDateTime.of(2025, 12, 2 + i % 5, 10, 0),
                    LocalDateTime.of(2025, 12, 2 + i % 5, 12, 0),
                    "메모 " + i,
                    (long) (i + 1),
                    place,
                    trip,
                    BigDecimal.valueOf(4.0)
            );
            ReflectionTestUtils.setField(tempSchedule, "id", (long) (i + 1));
            ReflectionTestUtils.setField(tempSchedule, "createdAt", LocalDateTime.of(2025, 12, 1, 9, i));
            schedules.add(tempSchedule);
        }

        given(scheduleRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(schedules);
        lenient().when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

        // when
        CursorPageResponseDto<ScheduleDto> result = scheduleService.getSchedules(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(scheduleRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("일정 목록 조회 성공 - 빈 목록")
    void getSchedules_Success_EmptyList() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(scheduleRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<ScheduleDto> result = scheduleService.getSchedules(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(scheduleRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("일정 목록 조회 성공 - 키워드 검색")
    void getSchedules_Success_WithKeyword() {
        // given
        Long tripId = 1L;
        String keyword = "검색";
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Schedule> schedules = List.of(schedule);
        given(scheduleRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(schedules);
        lenient().when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

        // when
        CursorPageResponseDto<ScheduleDto> result = scheduleService.getSchedules(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(scheduleRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("일정 단건 조회 성공")
    void getSchedule_Success() {
        // given
        Long tripId = 1L;
        Long scheduleId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        lenient().when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

        // when
        ScheduleDto result = scheduleService.getSchedule(tripId, scheduleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(scheduleDto);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
        then(scheduleMapper).should().toDto(any(Schedule.class));
    }

    @Test
    @DisplayName("일정 단건 조회 실패 - 여행을 찾을 수 없음")
    void getSchedule_TripNotFound() {
        // given
        Long tripId = 999L;
        Long scheduleId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.getSchedule(tripId, scheduleId))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("일정 단건 조회 실패 - 일정을 찾을 수 없음")
    void getSchedule_ScheduleNotFound() {
        // given
        Long tripId = 1L;
        Long scheduleId = 999L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.getSchedule(tripId, scheduleId))
                .isInstanceOf(ScheduleException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
    }

    @Test
    @DisplayName("일정 단건 조회 실패 - 일정이 여행에 속하지 않음")
    void getSchedule_ScheduleNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Trip anotherTrip = Trip.createTrip("다른 여행", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 10), null, null);
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        Schedule scheduleFromAnotherTrip = Schedule.createSchedule(
                "일정 제목",
                LocalDateTime.of(2025, 12, 2, 10, 0),
                LocalDateTime.of(2025, 12, 2, 12, 0),
                "메모",
                1L,
                place,
                anotherTrip,
                BigDecimal.valueOf(4.5)
        );

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(scheduleFromAnotherTrip));

        // when & then
        assertThatThrownBy(() -> scheduleService.getSchedule(tripId, scheduleId))
                .isInstanceOf(ScheduleException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
    }

    @Test
    @DisplayName("일정 수정 성공")
    void updateSchedule_Success() {
        // given
        Long tripId = 1L;
        Long scheduleId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(placeRepository.findById(eq(1L))).willReturn(Optional.of(place));
        lenient().when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

        // when
        ScheduleDto result = scheduleService.updateSchedule(tripId, scheduleId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(scheduleDto);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
        then(placeRepository).should().findById(eq(1L));
        then(scheduleMapper).should().toDto(any(Schedule.class));
    }

    @Test
    @DisplayName("일정 수정 성공 - 장소 없이")
    void updateSchedule_Success_WithoutPlace() {
        // given
        Long tripId = 1L;
        Long scheduleId = 1L;
        ScheduleUpdateRequestDto requestWithoutPlace = ScheduleUpdateRequestDto.builder()
                .title("수정된 일정 제목")
                .startDateTime(LocalDateTime.of(2025, 12, 2, 10, 0))
                .endDateTime(LocalDateTime.of(2025, 12, 2, 12, 0))
                .memo("수정된 메모")
                .rating(BigDecimal.valueOf(5.0))
                .placeId(null)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        lenient().when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

        // when
        ScheduleDto result = scheduleService.updateSchedule(tripId, scheduleId, requestWithoutPlace);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(scheduleDto);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
        then(scheduleMapper).should().toDto(any(Schedule.class));
    }

    @Test
    @DisplayName("일정 수정 실패 - 여행을 찾을 수 없음")
    void updateSchedule_TripNotFound() {
        // given
        Long tripId = 999L;
        Long scheduleId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(tripId, scheduleId, updateRequestDto))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("일정 수정 실패 - 일정을 찾을 수 없음")
    void updateSchedule_ScheduleNotFound() {
        // given
        Long tripId = 1L;
        Long scheduleId = 999L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(tripId, scheduleId, updateRequestDto))
                .isInstanceOf(ScheduleException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
    }

    @Test
    @DisplayName("일정 수정 실패 - 일정이 여행에 속하지 않음")
    void updateSchedule_ScheduleNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Trip anotherTrip = Trip.createTrip("다른 여행", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 10), null, null);
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        Schedule scheduleFromAnotherTrip = Schedule.createSchedule(
                "일정 제목",
                LocalDateTime.of(2025, 12, 2, 10, 0),
                LocalDateTime.of(2025, 12, 2, 12, 0),
                "메모",
                1L,
                place,
                anotherTrip,
                BigDecimal.valueOf(4.5)
        );

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(scheduleFromAnotherTrip));

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(tripId, scheduleId, updateRequestDto))
                .isInstanceOf(ScheduleException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
    }

    @Test
    @DisplayName("일정 수정 실패 - 장소를 찾을 수 없음")
    void updateSchedule_PlaceNotFound() {
        // given
        Long tripId = 1L;
        Long scheduleId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));
        given(placeRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(tripId, scheduleId, updateRequestDto))
                .isInstanceOf(PlaceException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
        then(placeRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("일정 삭제 성공")
    void deleteSchedule_Success() {
        // given
        Long tripId = 1L;
        Long scheduleId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(schedule));

        // when
        scheduleService.deleteSchedule(tripId, scheduleId);

        // then
        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
        then(scheduleRepository).should().delete(eq(schedule));
    }

    @Test
    @DisplayName("일정 삭제 실패 - 여행을 찾을 수 없음")
    void deleteSchedule_TripNotFound() {
        // given
        Long tripId = 999L;
        Long scheduleId = 1L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(tripId, scheduleId))
                .isInstanceOf(TripException.class);

        then(tripRepository).should().findById(eq(tripId));
    }

    @Test
    @DisplayName("일정 삭제 실패 - 일정을 찾을 수 없음")
    void deleteSchedule_ScheduleNotFound() {
        // given
        Long tripId = 1L;
        Long scheduleId = 999L;
        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(tripId, scheduleId))
                .isInstanceOf(ScheduleException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
    }

    @Test
    @DisplayName("일정 삭제 실패 - 일정이 여행에 속하지 않음")
    void deleteSchedule_ScheduleNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long scheduleId = 1L;
        Trip anotherTrip = Trip.createTrip("다른 여행", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 10), null, null);
        ReflectionTestUtils.setField(anotherTrip, "id", 2L);

        Schedule scheduleFromAnotherTrip = Schedule.createSchedule(
                "일정 제목",
                LocalDateTime.of(2025, 12, 2, 10, 0),
                LocalDateTime.of(2025, 12, 2, 12, 0),
                "메모",
                1L,
                place,
                anotherTrip,
                BigDecimal.valueOf(4.5)
        );

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(scheduleRepository.findById(eq(scheduleId))).willReturn(Optional.of(scheduleFromAnotherTrip));

        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(tripId, scheduleId))
                .isInstanceOf(ScheduleException.class);

        then(tripRepository).should().findById(eq(tripId));
        then(scheduleRepository).should().findById(eq(scheduleId));
    }
}
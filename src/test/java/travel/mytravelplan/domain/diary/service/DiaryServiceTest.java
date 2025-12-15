package travel.mytravelplan.domain.diary.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.diary.dto.DiaryCreateRequestDto;
import travel.mytravelplan.domain.diary.dto.DiaryDto;
import travel.mytravelplan.domain.diary.dto.DiaryUpdateRequestDto;
import travel.mytravelplan.domain.diary.entity.Diary;
import travel.mytravelplan.domain.diary.enums.Emotion;
import travel.mytravelplan.domain.diary.exception.DiaryException;
import travel.mytravelplan.domain.diary.mapper.DiaryMapper;
import travel.mytravelplan.domain.diary.repository.DiaryRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.exception.TripJoinException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.DiaryErrorCode;
import travel.mytravelplan.global.error.code.TripErrorCode;
import travel.mytravelplan.global.error.code.TripJoinErrorCode;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("일기 서비스 테스트")
class DiaryServiceTest extends ServiceTestSupport {

    @InjectMocks
    private DiaryService diaryService;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DiaryMapper diaryMapper;

    @Mock
    private TripJoinRepository tripJoinRepository;

    private Trip trip;
    private User user;
    private TripJoin tripJoin;
    private Diary diary;
    private DiaryDto diaryDto;

    @BeforeEach
    void setUp() {
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);
        trip = Trip.createTrip("제주도 여행", null, null, null, null);
        tripJoin = TripJoin.createTripJoin(trip, user);

        diary = Diary.createDiary(
                "첫 일기",
                "오늘은 제주도에 도착했다.",
                List.of("image1.jpg", "image2.jpg"),
                LocalDate.of(2025, 11, 27),
                Emotion.HAPPY,
                trip,
                tripJoin
        );
        ReflectionTestUtils.setField(diary, "id", 1L);
        ReflectionTestUtils.setField(diary, "createdAt", LocalDateTime.of(2025, 11, 27, 10, 0));

        diaryDto = DiaryDto.builder()
                .id(1L)
                .title("첫 일기")
                .content("오늘은 제주도에 도착했다.")
                .imageUrls(List.of("image1.jpg", "image2.jpg"))
                .date(LocalDate.of(2025, 11, 27))
                .emotion(Emotion.HAPPY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("일기를 생성한다")
    void createDiary() {
        // given
        Long tripId = 1L;
        DiaryCreateRequestDto requestDto = DiaryCreateRequestDto.builder()
                .title("첫 일기")
                .content("오늘은 제주도에 도착했다.")
                .imageUrls(List.of("image1.jpg", "image2.jpg"))
                .date(LocalDate.of(2025, 11, 27))
                .emotion(Emotion.HAPPY)
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByUserAndTrip(eq(user), eq(trip))).willReturn(Optional.of(tripJoin));
        given(diaryRepository.save(any(Diary.class))).willReturn(diary);
        given(diaryMapper.toDto(any(Diary.class))).willReturn(diaryDto);

        // when
        DiaryDto result = diaryService.createDiary(tripId, user, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("첫 일기");
        assertThat(result.getContent()).isEqualTo("오늘은 제주도에 도착했다.");
        assertThat(result.getImageUrls()).hasSize(2);
        assertThat(result.getEmotion()).isEqualTo(Emotion.HAPPY);
        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should().findByUserAndTrip(eq(user), eq(trip));
        then(diaryRepository).should().save(any(Diary.class));
        then(diaryMapper).should().toDto(any(Diary.class));
    }

    @Test
    @DisplayName("일기 생성 시 여행이 존재하지 않으면 예외를 던진다")
    void createDiary_TripNotFound() {
        // given
        Long tripId = 999L;
        DiaryCreateRequestDto requestDto = DiaryCreateRequestDto.builder()
                .title("첫 일기")
                .content("오늘은 제주도에 도착했다.")
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(tripId, user, requestDto))
                .isInstanceOf(TripException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripErrorCode.TRIP_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should(never()).findByUserAndTrip(any(), any());
        then(diaryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("일기 생성 시 여행 참여자가 아니면 예외를 던진다")
    void createDiary_TripJoinNotFound() {
        // given
        Long tripId = 1L;
        DiaryCreateRequestDto requestDto = DiaryCreateRequestDto.builder()
                .title("첫 일기")
                .content("오늘은 제주도에 도착했다.")
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripJoinRepository.findByUserAndTrip(eq(user), eq(trip))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(tripId, user, requestDto))
                .isInstanceOf(TripJoinException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripJoinErrorCode.TRIP_JOIN_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(tripJoinRepository).should().findByUserAndTrip(eq(user), eq(trip));
        then(diaryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("커서 기반으로 일기 목록을 조회한다")
    void getDiaries() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Diary> diaries = List.of(diary);

        given(diaryRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(diaries);
        given(diaryMapper.toDto(any(Diary.class))).willReturn(diaryDto);

        // when
        CursorPageResponseDto<DiaryDto> result = diaryService.getDiaries(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        then(diaryRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("커서 기반으로 일기 목록을 조회할 때 다음 페이지가 있으면 커서 정보를 반환한다")
    void getDiaries_WithNextPage() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        Diary diary2 = Diary.createDiary(
                "두 번째 일기",
                "두 번째 내용",
                new ArrayList<>(),
                LocalDate.of(2025, 11, 28),
                Emotion.EXCITED,
                trip,
                tripJoin
        );
        ReflectionTestUtils.setField(diary2, "id", 2L);
        ReflectionTestUtils.setField(diary2, "createdAt", LocalDateTime.of(2025, 11, 28, 10, 0));

        List<Diary> diaries = List.of(diary, diary2);

        given(diaryRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(diaries);
        given(diaryMapper.toDto(any(Diary.class))).willReturn(diaryDto);

        // when
        CursorPageResponseDto<DiaryDto> result = diaryService.getDiaries(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();
        then(diaryRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("일기 목록 조회 시 빈 목록을 반환한다")
    void getDiaries_EmptyList() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(diaryRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<DiaryDto> result = diaryService.getDiaries(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(diaryRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("일기 목록 조회 시 여러 일기를 반환하고 다음 페이지가 없는 경우")
    void getDiaries_MultipleItems_NoNext() {
        // given
        Long tripId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        Diary diary2 = Diary.createDiary(
                "두 번째 일기",
                "두 번째 내용",
                new ArrayList<>(),
                LocalDate.of(2025, 11, 28),
                Emotion.EXCITED,
                trip,
                tripJoin
        );
        ReflectionTestUtils.setField(diary2, "id", 2L);
        ReflectionTestUtils.setField(diary2, "createdAt", LocalDateTime.of(2025, 11, 28, 10, 0));

        List<Diary> diaries = List.of(diary, diary2);

        DiaryDto diaryDto2 = DiaryDto.builder()
                .id(2L)
                .title("두 번째 일기")
                .content("두 번째 내용")
                .imageUrls(new ArrayList<>())
                .date(LocalDate.of(2025, 11, 28))
                .emotion(Emotion.EXCITED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(diaryRepository.findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(diaries);
        given(diaryMapper.toDto(eq(diary))).willReturn(diaryDto);
        given(diaryMapper.toDto(eq(diary2))).willReturn(diaryDto2);

        // when
        CursorPageResponseDto<DiaryDto> result = diaryService.getDiaries(tripId, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getHasNext()).isFalse();

        then(diaryRepository).should().findAllByCursor(eq(tripId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(diaryMapper).should().toDto(eq(diary));
        then(diaryMapper).should().toDto(eq(diary2));
    }

    @Test
    @DisplayName("일기를 단건 조회한다")
    void getDiary() {
        // given
        Long tripId = 1L;
        Long diaryId = 1L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.of(diary));
        given(diaryMapper.toDto(eq(diary))).willReturn(diaryDto);

        // when
        DiaryDto result = diaryService.getDiary(tripId, diaryId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("첫 일기");
        assertThat(result.getContent()).isEqualTo("오늘은 제주도에 도착했다.");
        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
        then(diaryMapper).should().toDto(eq(diary));
    }

    @Test
    @DisplayName("일기 단건 조회 시 여행이 존재하지 않으면 예외를 던진다")
    void getDiary_TripNotFound() {
        // given
        Long tripId = 999L;
        Long diaryId = 1L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.getDiary(tripId, diaryId))
                .isInstanceOf(TripException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripErrorCode.TRIP_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("일기 단건 조회 시 일기가 존재하지 않으면 예외를 던진다")
    void getDiary_DiaryNotFound() {
        // given
        Long tripId = 1L;
        Long diaryId = 999L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.getDiary(tripId, diaryId))
                .isInstanceOf(DiaryException.class)
                .hasFieldOrPropertyWithValue("errorCode", DiaryErrorCode.DIARY_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
    }

    @Test
    @DisplayName("일기 단건 조회 시 일기가 여행에 속하지 않으면 예외를 던진다")
    void getDiary_DiaryNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long diaryId = 1L;

        Trip anotherTrip = Trip.createTrip("부산 여행", null, null, null, null);

        Diary anotherDiary = Diary.createDiary(
                "다른 일기",
                "다른 내용",
                new ArrayList<>(),
                LocalDate.of(2025, 11, 27),
                Emotion.HAPPY,
                anotherTrip,
                tripJoin
        );

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.of(anotherDiary));

        // when & then
        assertThatThrownBy(() -> diaryService.getDiary(tripId, diaryId))
                .isInstanceOf(DiaryException.class)
                .hasFieldOrPropertyWithValue("errorCode", DiaryErrorCode.DIARY_NOT_BELONG_TO_TRIP);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
    }

    @Test
    @DisplayName("일기를 수정한다")
    void updateDiary() {
        // given
        Long tripId = 1L;
        Long diaryId = 1L;

        DiaryUpdateRequestDto requestDto = DiaryUpdateRequestDto.builder()
                .title("수정된 일기")
                .content("수정된 내용")
                .imageUrls(List.of("updated1.jpg"))
                .date(LocalDate.of(2025, 11, 28))
                .emotion(Emotion.PEACEFUL)
                .build();

        DiaryDto updatedDiaryDto = DiaryDto.builder()
                .id(1L)
                .title("수정된 일기")
                .content("수정된 내용")
                .imageUrls(List.of("updated1.jpg"))
                .date(LocalDate.of(2025, 11, 28))
                .emotion(Emotion.PEACEFUL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.of(diary));
        given(diaryMapper.toDto(eq(diary))).willReturn(updatedDiaryDto);

        // when
        DiaryDto result = diaryService.updateDiary(tripId, diaryId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("수정된 일기");
        assertThat(result.getContent()).isEqualTo("수정된 내용");
        assertThat(result.getEmotion()).isEqualTo(Emotion.PEACEFUL);
        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
        then(diaryMapper).should().toDto(eq(diary));
    }

    @Test
    @DisplayName("일기 수정 시 여행이 존재하지 않으면 예외를 던진다")
    void updateDiary_TripNotFound() {
        // given
        Long tripId = 999L;
        Long diaryId = 1L;

        DiaryUpdateRequestDto requestDto = DiaryUpdateRequestDto.builder()
                .title("수정된 일기")
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(tripId, diaryId, requestDto))
                .isInstanceOf(TripException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripErrorCode.TRIP_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("일기 수정 시 일기가 존재하지 않으면 예외를 던진다")
    void updateDiary_DiaryNotFound() {
        // given
        Long tripId = 1L;
        Long diaryId = 999L;

        DiaryUpdateRequestDto requestDto = DiaryUpdateRequestDto.builder()
                .title("수정된 일기")
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(tripId, diaryId, requestDto))
                .isInstanceOf(DiaryException.class)
                .hasFieldOrPropertyWithValue("errorCode", DiaryErrorCode.DIARY_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
    }

    @Test
    @DisplayName("일기 수정 시 일기가 여행에 속하지 않으면 예외를 던진다")
    void updateDiary_DiaryNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long diaryId = 1L;

        Trip anotherTrip = Trip.createTrip("부산 여행", null, null, null, null);

        Diary anotherDiary = Diary.createDiary(
                "다른 일기",
                "다른 내용",
                new ArrayList<>(),
                LocalDate.of(2025, 11, 27),
                Emotion.HAPPY,
                anotherTrip,
                tripJoin
        );

        DiaryUpdateRequestDto requestDto = DiaryUpdateRequestDto.builder()
                .title("수정된 일기")
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.of(anotherDiary));

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(tripId, diaryId, requestDto))
                .isInstanceOf(DiaryException.class)
                .hasFieldOrPropertyWithValue("errorCode", DiaryErrorCode.DIARY_NOT_BELONG_TO_TRIP);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
    }

    @Test
    @DisplayName("일기를 삭제한다")
    void deleteDiary() {
        // given
        Long tripId = 1L;
        Long diaryId = 1L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.of(diary));

        // when
        diaryService.deleteDiary(tripId, diaryId);

        // then
        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
        then(diaryRepository).should().delete(eq(diary));
    }

    @Test
    @DisplayName("일기 삭제 시 여행이 존재하지 않으면 예외를 던진다")
    void deleteDiary_TripNotFound() {
        // given
        Long tripId = 999L;
        Long diaryId = 1L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(tripId, diaryId))
                .isInstanceOf(TripException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripErrorCode.TRIP_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should(never()).findById(any());
        then(diaryRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("일기 삭제 시 일기가 존재하지 않으면 예외를 던진다")
    void deleteDiary_DiaryNotFound() {
        // given
        Long tripId = 1L;
        Long diaryId = 999L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(tripId, diaryId))
                .isInstanceOf(DiaryException.class)
                .hasFieldOrPropertyWithValue("errorCode", DiaryErrorCode.DIARY_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
        then(diaryRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("일기 삭제 시 일기가 여행에 속하지 않으면 예외를 던진다")
    void deleteDiary_DiaryNotBelongToTrip() {
        // given
        Long tripId = 1L;
        Long diaryId = 1L;

        Trip anotherTrip = Trip.createTrip("부산 여행", null, null, null, null);

        Diary anotherDiary = Diary.createDiary(
                "다른 일기",
                "다른 내용",
                new ArrayList<>(),
                LocalDate.of(2025, 11, 27),
                Emotion.HAPPY,
                anotherTrip,
                tripJoin
        );

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(diaryRepository.findById(eq(diaryId))).willReturn(Optional.of(anotherDiary));

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(tripId, diaryId))
                .isInstanceOf(DiaryException.class)
                .hasFieldOrPropertyWithValue("errorCode", DiaryErrorCode.DIARY_NOT_BELONG_TO_TRIP);

        then(tripRepository).should().findById(eq(tripId));
        then(diaryRepository).should().findById(eq(diaryId));
        then(diaryRepository).should(never()).delete(any());
    }
}
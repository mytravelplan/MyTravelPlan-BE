package travel.mytravelplan.domain.place.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.place.dto.TripPlaceBookMarkDto;
import travel.mytravelplan.domain.place.dto.TripPlaceCreateRequestDto;
import travel.mytravelplan.domain.place.dto.TripPlaceDto;
import travel.mytravelplan.domain.place.dto.TripPlaceUpdateRequestDto;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.entity.TripPlaceBookMark;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.enums.PlaceType;
import travel.mytravelplan.domain.place.exception.TripPlaceException;
import travel.mytravelplan.domain.place.mapper.TripPlaceBookMarkMapper;
import travel.mytravelplan.domain.place.mapper.TripPlaceMapper;
import travel.mytravelplan.domain.place.repository.TripPlaceBookMarkRepository;
import travel.mytravelplan.domain.place.repository.TripPlaceRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("여행 장소 서비스 테스트")
class TripPlaceServiceTest extends ServiceTestSupport {

    @Mock
    private TripPlaceRepository tripPlaceRepository;

    @Mock
    private TripPlaceBookMarkRepository tripPlaceBookMarkRepository;

    @Mock
    private TripPlaceMapper tripPlaceMapper;

    @Mock
    private TripPlaceBookMarkMapper tripPlaceBookMarkMapper;

    @InjectMocks
    private TripPlaceService tripPlaceService;

    private User user;
    private TripPlace tripPlace;
    private TripPlaceDto tripPlaceDto;

    @BeforeEach
    void setUp() {
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);

        tripPlace = TripPlace.createTripPlace(
                "여행지",
                "서울시 강남구",
                "멋진 여행지",
                new BigDecimal("37.123456"),
                new BigDecimal("127.123456"),
                PlaceCategory.ATTRACTION,
                "https://example.com"
        );

        tripPlaceDto = TripPlaceDto.builder()
                .id(1L)
                .name("여행지")
                .address("서울시 강남구")
                .description("멋진 여행지")
                .latitude(new BigDecimal("37.123456"))
                .longitude(new BigDecimal("127.123456"))
                .externalUrl("https://example.com")
                .build();
    }

    @Test
    @DisplayName("여행 장소를 생성한다")
    void createTripPlace() {
        // given
        TripPlaceCreateRequestDto requestDto = TripPlaceCreateRequestDto.builder()
                .placeType(PlaceType.TRIP)
                .name("여행지")
                .address("서울시 강남구")
                .description("멋진 여행지")
                .latitude(new BigDecimal("37.123456"))
                .longitude(new BigDecimal("127.123456"))
                .category(PlaceCategory.ATTRACTION)
                .externalUrl("https://example.com")
                .build();

        given(tripPlaceRepository.save(any(TripPlace.class))).willReturn(tripPlace);
        given(tripPlaceMapper.toDto(any(TripPlace.class), eq(user))).willReturn(tripPlaceDto);

        // when
        TripPlaceDto result = tripPlaceService.createTripPlace(user, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("여행지");
        assertThat(result.getAddress()).isEqualTo("서울시 강남구");
        assertThat(result.getExternalUrl()).isEqualTo("https://example.com");
        then(tripPlaceRepository).should().save(any(TripPlace.class));
        then(tripPlaceMapper).should().toDto(any(TripPlace.class), eq(user));
    }

    @Test
    @DisplayName("커서 기반으로 여행 장소 목록을 조회한다")
    void getTripPlaces() {
        // given
        String keyword = "여행";
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        TripPlace tripPlace2 = TripPlace.createTripPlace(
                "여행지2",
                "서울시 서초구",
                "멋진 여행지2",
                new BigDecimal("37.234567"),
                new BigDecimal("127.234567"),
                PlaceCategory.RESTAURANT,
                "https://example2.com"
        );

        List<TripPlace> tripPlaces = List.of(tripPlace, tripPlace2);

        TripPlaceDto tripPlaceDto2 = TripPlaceDto.builder()
                .id(2L)
                .name("여행지2")
                .address("서울시 서초구")
                .description("멋진 여행지2")
                .latitude(new BigDecimal("37.234567"))
                .longitude(new BigDecimal("127.234567"))
                .externalUrl("https://example2.com")
                .build();

        given(tripPlaceRepository.findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(tripPlaces);
        given(tripPlaceMapper.toDto(eq(tripPlace), eq(user))).willReturn(tripPlaceDto);
        given(tripPlaceMapper.toDto(eq(tripPlace2), eq(user))).willReturn(tripPlaceDto2);

        // when
        CursorPageResponseDto<TripPlaceDto> result = tripPlaceService.getTripPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("여행지");
        assertThat(result.getContent().get(1).getName()).isEqualTo("여행지2");
        assertThat(result.getHasNext()).isFalse();
        then(tripPlaceRepository).should().findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("hasNext가 true인 경우 다음 페이지가 있음을 표시한다")
    void getTripPlaces_HasNext() {
        // given
        String keyword = "여행";
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 2;

        TripPlace tripPlace2 = TripPlace.createTripPlace(
                "여행지2",
                "서울시 서초구",
                "멋진 여행지2",
                new BigDecimal("37.234567"),
                new BigDecimal("127.234567"),
                PlaceCategory.RESTAURANT,
                "https://example2.com"
        );

        TripPlace tripPlace3 = TripPlace.createTripPlace(
                "여행지3",
                "서울시 종로구",
                "멋진 여행지3",
                new BigDecimal("37.345678"),
                new BigDecimal("127.345678"),
                PlaceCategory.CAFE,
                "https://example3.com"
        );

        ReflectionTestUtils.setField(tripPlace, "id", 1L);
        ReflectionTestUtils.setField(tripPlace, "createdAt", LocalDateTime.of(2023, 12, 1, 10, 0, 0));
        ReflectionTestUtils.setField(tripPlace2, "id", 2L);
        ReflectionTestUtils.setField(tripPlace2, "createdAt", LocalDateTime.of(2023, 12, 1, 9, 0, 0));
        ReflectionTestUtils.setField(tripPlace3, "id", 3L);
        ReflectionTestUtils.setField(tripPlace3, "createdAt", LocalDateTime.of(2023, 12, 1, 8, 0, 0));

        // limit+1 = 3개를 반환
        List<TripPlace> tripPlaces = List.of(tripPlace, tripPlace2, tripPlace3);

        TripPlaceDto tripPlaceDto2 = TripPlaceDto.builder()
                .id(2L)
                .name("여행지2")
                .address("서울시 서초구")
                .description("멋진 여행지2")
                .latitude(new BigDecimal("37.234567"))
                .longitude(new BigDecimal("127.234567"))
                .externalUrl("https://example2.com")
                .build();

        given(tripPlaceRepository.findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(tripPlaces);
        given(tripPlaceMapper.toDto(eq(tripPlace), eq(user))).willReturn(tripPlaceDto);
        given(tripPlaceMapper.toDto(eq(tripPlace2), eq(user))).willReturn(tripPlaceDto2);

        // when
        CursorPageResponseDto<TripPlaceDto> result = tripPlaceService.getTripPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2); // limit만큼만 반환
        assertThat(result.getHasNext()).isTrue(); // 다음 페이지가 있음
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isEqualTo(2L);
        assertThat(result.getSize()).isEqualTo(2);
        then(tripPlaceRepository).should().findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("빈 결과를 반환한다")
    void getTripPlaces_EmptyResult() {
        // given
        String keyword = "존재하지않는여행지";
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<TripPlace> tripPlaces = List.of();

        given(tripPlaceRepository.findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(tripPlaces);

        // when
        CursorPageResponseDto<TripPlaceDto> result = tripPlaceService.getTripPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        assertThat(result.getSize()).isEqualTo(0);
        then(tripPlaceRepository).should().findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("커서와 after 값으로 다음 페이지를 조회한다")
    void getTripPlaces_WithCursorAndAfter() {
        // given
        String keyword = "여행";
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = "2023-12-01T10:00:00";
        Long after = 5L;
        int limit = 10;

        TripPlace tripPlace2 = TripPlace.createTripPlace(
                "여행지2",
                "서울시 서초구",
                "멋진 여행지2",
                new BigDecimal("37.234567"),
                new BigDecimal("127.234567"),
                PlaceCategory.RESTAURANT,
                "https://example2.com"
        );

        List<TripPlace> tripPlaces = List.of(tripPlace2);

        TripPlaceDto tripPlaceDto2 = TripPlaceDto.builder()
                .id(6L)
                .name("여행지2")
                .address("서울시 서초구")
                .description("멋진 여행지2")
                .latitude(new BigDecimal("37.234567"))
                .longitude(new BigDecimal("127.234567"))
                .externalUrl("https://example2.com")
                .build();

        given(tripPlaceRepository.findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(tripPlaces);
        given(tripPlaceMapper.toDto(eq(tripPlace2), eq(user))).willReturn(tripPlaceDto2);

        // when
        CursorPageResponseDto<TripPlaceDto> result = tripPlaceService.getTripPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        then(tripPlaceRepository).should().findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("정확히 limit만큼의 결과를 반환한다")
    void getTripPlaces_ExactLimit() {
        // given
        String keyword = "여행";
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 1;

        List<TripPlace> tripPlaces = List.of(tripPlace);

        given(tripPlaceRepository.findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(tripPlaces);
        given(tripPlaceMapper.toDto(eq(tripPlace), eq(user))).willReturn(tripPlaceDto);

        // when
        CursorPageResponseDto<TripPlaceDto> result = tripPlaceService.getTripPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        assertThat(result.getSize()).isEqualTo(1);
        then(tripPlaceRepository).should().findAllByCursor(eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("ID로 여행 장소를 조회한다")
    void getTripPlace() {
        // given
        Long tripPlaceId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceMapper.toDto(eq(tripPlace), eq(user))).willReturn(tripPlaceDto);

        // when
        TripPlaceDto result = tripPlaceService.getTripPlace(user, tripPlaceId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("여행지");
        assertThat(result.getAddress()).isEqualTo("서울시 강남구");
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceMapper).should().toDto(eq(tripPlace), eq(user));
    }

    @Test
    @DisplayName("존재하지 않는 여행 장소 조회 시 예외가 발생한다")
    void getTripPlace_NotFound() {
        // given
        Long tripPlaceId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceService.getTripPlace(user, tripPlaceId))
                .isInstanceOf(TripPlaceException.class);
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소를 수정한다")
    void updateTripPlace() {
        // given
        Long tripPlaceId = 1L;
        TripPlaceUpdateRequestDto requestDto = TripPlaceUpdateRequestDto.builder()
                .name("수정된 여행지")
                .address("서울시 송파구")
                .description("수정된 설명")
                .latitude(new BigDecimal("37.345678"))
                .longitude(new BigDecimal("127.345678"))
                .category(PlaceCategory.SHOPPING)
                .externalUrl("https://updated.com")
                .build();

        TripPlaceDto updatedDto = TripPlaceDto.builder()
                .id(1L)
                .name("수정된 여행지")
                .address("서울시 송파구")
                .description("수정된 설명")
                .latitude(new BigDecimal("37.345678"))
                .longitude(new BigDecimal("127.345678"))
                .externalUrl("https://updated.com")
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceMapper.toDto(eq(tripPlace), eq(user))).willReturn(updatedDto);

        // when
        TripPlaceDto result = tripPlaceService.updateTripPlace(user, tripPlaceId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("수정된 여행지");
        assertThat(result.getAddress()).isEqualTo("서울시 송파구");
        assertThat(result.getExternalUrl()).isEqualTo("https://updated.com");
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceMapper).should().toDto(eq(tripPlace), eq(user));
    }

    @Test
    @DisplayName("존재하지 않는 여행 장소 수정 시 예외가 발생한다")
    void updateTripPlace_NotFound() {
        // given
        Long tripPlaceId = 999L;
        TripPlaceUpdateRequestDto requestDto = TripPlaceUpdateRequestDto.builder()
                .name("수정된 여행지")
                .address("서울시 송파구")
                .description("수정된 설명")
                .latitude(new BigDecimal("37.345678"))
                .longitude(new BigDecimal("127.345678"))
                .category(PlaceCategory.SHOPPING)
                .externalUrl("https://updated.com")
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceService.updateTripPlace(user, tripPlaceId, requestDto))
                .isInstanceOf(TripPlaceException.class);
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소를 삭제한다")
    void deleteTripPlace() {
        // given
        Long tripPlaceId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));

        // when
        tripPlaceService.deleteTripPlace(tripPlaceId);

        // then
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceRepository).should().delete(eq(tripPlace));
    }

    @Test
    @DisplayName("존재하지 않는 여행 장소 삭제 시 예외가 발생한다")
    void deleteTripPlace_NotFound() {
        // given
        Long tripPlaceId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceService.deleteTripPlace(tripPlaceId))
                .isInstanceOf(TripPlaceException.class);
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소에 북마크를 추가한다")
    void bookmarkTripPlace_Add() {
        // given
        Long tripPlaceId = 1L;
        TripPlaceBookMark tripPlaceBookMark = TripPlaceBookMark.createTripPlaceBookMark(tripPlace, user);
        TripPlaceBookMarkDto bookMarkDto = TripPlaceBookMarkDto.builder()
                .tripPlaceId(tripPlaceId)
                .userId(1L)
                .bookmarked(true)
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceBookMarkRepository.findByTripPlaceAndUser(eq(tripPlace), eq(user))).willReturn(Optional.empty());
        given(tripPlaceBookMarkRepository.save(any(TripPlaceBookMark.class))).willReturn(tripPlaceBookMark);
        given(tripPlaceBookMarkMapper.toDto(any(TripPlaceBookMark.class), eq(true))).willReturn(bookMarkDto);

        // when
        TripPlaceBookMarkDto result = tripPlaceService.bookmarkTripPlace(user, tripPlaceId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isBookmarked()).isTrue();
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceBookMarkRepository).should().findByTripPlaceAndUser(eq(tripPlace), eq(user));
        then(tripPlaceBookMarkRepository).should().save(any(TripPlaceBookMark.class));
    }

    @Test
    @DisplayName("여행 장소의 북마크를 제거한다")
    void bookmarkTripPlace_Remove() {
        // given
        Long tripPlaceId = 1L;
        TripPlaceBookMark tripPlaceBookMark = TripPlaceBookMark.createTripPlaceBookMark(tripPlace, user);
        TripPlaceBookMarkDto bookMarkDto = TripPlaceBookMarkDto.builder()
                .tripPlaceId(tripPlaceId)
                .userId(1L)
                .bookmarked(false)
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceBookMarkRepository.findByTripPlaceAndUser(eq(tripPlace), eq(user))).willReturn(Optional.of(tripPlaceBookMark));
        given(tripPlaceBookMarkMapper.toDto(eq(tripPlaceBookMark), eq(false))).willReturn(bookMarkDto);

        // when
        TripPlaceBookMarkDto result = tripPlaceService.bookmarkTripPlace(user, tripPlaceId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isBookmarked()).isFalse();
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceBookMarkRepository).should().findByTripPlaceAndUser(eq(tripPlace), eq(user));
        then(tripPlaceBookMarkRepository).should().delete(eq(tripPlaceBookMark));
    }

    @Test
    @DisplayName("존재하지 않는 여행 장소에 북마크 시 예외가 발생한다")
    void bookmarkTripPlace_NotFound() {
        // given
        Long tripPlaceId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceService.bookmarkTripPlace(user, tripPlaceId))
                .isInstanceOf(TripPlaceException.class);
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }
}
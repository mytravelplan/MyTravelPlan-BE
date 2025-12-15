package travel.mytravelplan.domain.trip.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.currency.entity.Currency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.repository.CurrencyRepository;
import travel.mytravelplan.domain.currency.repository.TripCurrencyRepository;
import travel.mytravelplan.domain.trip.dto.TripCreateRequestDto;
import travel.mytravelplan.domain.trip.dto.TripDto;
import travel.mytravelplan.domain.trip.dto.TripUpdateRequestDto;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.mapper.TripMapper;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.TripErrorCode;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("여행 서비스 테스트")
class TripServiceTest extends ServiceTestSupport {

    @InjectMocks
    private TripService tripService;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripCurrencyRepository tripCurrencyRepository;

    @Mock
    private TripJoinRepository tripJoinRepository;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private CurrencyRepository currencyRepository;

    private User user;
    private Trip trip;
    private TripDto tripDto;

    @BeforeEach
    void setUp() {
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);
        ReflectionTestUtils.setField(user, "id", 1L);

        trip = Trip.createTrip(
                "일본 여행",
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 10),
                "image.jpg",
                Set.of(Country.JP)
        );
        ReflectionTestUtils.setField(trip, "id", 1L);
        ReflectionTestUtils.setField(trip, "createdAt", LocalDateTime.of(2025, 11, 27, 10, 0));

        tripDto = TripDto.builder()
                .id(1L)
                .title("일본 여행")
                .startDate(LocalDate.of(2025, 12, 1))
                .endDate(LocalDate.of(2025, 12, 10))
                .imageUrl("image.jpg")
                .build();
    }

    @Test
    @DisplayName("여행을 생성한다")
    void createTrip() {
        // given
        TripCreateRequestDto requestDto = TripCreateRequestDto.builder()
                .title("일본 여행")
                .startDate(LocalDate.of(2025, 12, 1))
                .endDate(LocalDate.of(2025, 12, 10))
                .imageUrl("image.jpg")
                .countries(Set.of(Country.JP))
                .build();

        Currency krwCurrency = Currency.createCurrency(CurrencyType.KRW, BigDecimal.ONE);
        Currency usdCurrency = Currency.createCurrency(CurrencyType.USD, BigDecimal.valueOf(1350));
        Currency jpyCurrency = Currency.createCurrency(CurrencyType.JPY, BigDecimal.valueOf(900));

        given(tripRepository.save(any(Trip.class))).willReturn(trip);
        given(tripJoinRepository.save(any(TripJoin.class))).willReturn(TripJoin.createTripJoin(trip, user));
        given(currencyRepository.findByCurrencyTypeIn(any())).willReturn(List.of(krwCurrency, usdCurrency, jpyCurrency));
        given(tripCurrencyRepository.saveAll(any())).willReturn(List.of());
        given(tripMapper.toDto(any(Trip.class))).willReturn(tripDto);

        // when
        TripDto result = tripService.createTrip(user, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("일본 여행");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 12, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 12, 10));
        assertThat(result.getImageUrl()).isEqualTo("image.jpg");
        then(tripRepository).should().save(any(Trip.class));
        then(tripJoinRepository).should().save(any(TripJoin.class));
        then(currencyRepository).should().findByCurrencyTypeIn(any());
        then(tripCurrencyRepository).should().saveAll(any());
        then(tripMapper).should().toDto(any(Trip.class));
    }

    @Test
    @DisplayName("커서 기반으로 사용자의 여행 목록을 조회한다")
    void getUserTrips() {
        // given
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Trip> trips = List.of(trip);

        given(tripRepository.findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(trips);
        given(tripMapper.toDto(any(Trip.class))).willReturn(tripDto);

        // when
        CursorPageResponseDto<TripDto> result = tripService.getUserTrips(user, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        then(tripRepository).should().findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("커서 기반으로 사용자의 여행 목록을 조회할 때 다음 페이지가 있으면 커서 정보를 반환한다")
    void getUserTrips_WithNextPage() {
        // given
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        Trip trip2 = Trip.createTrip(
                "프랑스 여행",
                LocalDate.of(2025, 12, 15),
                LocalDate.of(2025, 12, 25),
                "image2.jpg",
                Set.of(Country.FR)
        );
        ReflectionTestUtils.setField(trip2, "id", 2L);
        ReflectionTestUtils.setField(trip2, "createdAt", LocalDateTime.of(2025, 11, 28, 10, 0));

        List<Trip> trips = List.of(trip, trip2);

        given(tripRepository.findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(trips);
        given(tripMapper.toDto(any(Trip.class))).willReturn(tripDto);

        // when
        CursorPageResponseDto<TripDto> result = tripService.getUserTrips(user, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();
        then(tripRepository).should().findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("여행을 단건 조회한다")
    void getTrip() {
        // given
        Long tripId = 1L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripMapper.toDto(eq(trip))).willReturn(tripDto);

        // when
        TripDto result = tripService.getTrip(tripId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("일본 여행");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 12, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 12, 10));
        then(tripRepository).should().findById(eq(tripId));
        then(tripMapper).should().toDto(eq(trip));
    }

    @Test
    @DisplayName("여행 단건 조회 시 여행이 존재하지 않으면 예외를 던진다")
    void getTrip_TripNotFound() {
        // given
        Long tripId = 999L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripService.getTrip(tripId))
                .isInstanceOf(TripException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripErrorCode.TRIP_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(tripMapper).should(never()).toDto(any());
    }

    @Test
    @DisplayName("여행을 수정한다")
    void updateTrip() {
        // given
        Long tripId = 1L;
        TripUpdateRequestDto requestDto = TripUpdateRequestDto.builder()
                .title("수정된 일본 여행")
                .startDate(LocalDate.of(2025, 12, 2))
                .endDate(LocalDate.of(2025, 12, 11))
                .imageUrl("updated_image.jpg")
                .countries(Set.of(Country.JP, Country.KR))
                .build();

        TripDto updatedTripDto = TripDto.builder()
                .id(1L)
                .title("수정된 일본 여행")
                .startDate(LocalDate.of(2025, 12, 2))
                .endDate(LocalDate.of(2025, 12, 11))
                .imageUrl("updated_image.jpg")
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));
        given(tripMapper.toDto(any(Trip.class))).willReturn(updatedTripDto);

        // when
        TripDto result = tripService.updateTrip(tripId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("수정된 일본 여행");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 12, 2));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 12, 11));
        assertThat(result.getImageUrl()).isEqualTo("updated_image.jpg");
        then(tripRepository).should().findById(eq(tripId));
        then(tripMapper).should().toDto(any(Trip.class));
    }

    @Test
    @DisplayName("여행 수정 시 여행이 존재하지 않으면 예외를 던진다")
    void updateTrip_TripNotFound() {
        // given
        Long tripId = 999L;
        TripUpdateRequestDto requestDto = TripUpdateRequestDto.builder()
                .title("수정된 일본 여행")
                .startDate(LocalDate.of(2025, 12, 2))
                .endDate(LocalDate.of(2025, 12, 11))
                .imageUrl("updated_image.jpg")
                .countries(Set.of(Country.JP))
                .build();

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripService.updateTrip(tripId, requestDto))
                .isInstanceOf(TripException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripErrorCode.TRIP_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(tripMapper).should(never()).toDto(any());
    }

    @Test
    @DisplayName("여행을 삭제한다")
    void deleteTrip() {
        // given
        Long tripId = 1L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.of(trip));

        // when
        tripService.deleteTrip(tripId);

        // then
        then(tripRepository).should().findById(eq(tripId));
        then(tripRepository).should().delete(eq(trip));
    }

    @Test
    @DisplayName("여행 삭제 시 여행이 존재하지 않으면 예외를 던진다")
    void deleteTrip_TripNotFound() {
        // given
        Long tripId = 999L;

        given(tripRepository.findById(eq(tripId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripService.deleteTrip(tripId))
                .isInstanceOf(TripException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripErrorCode.TRIP_NOT_FOUND);

        then(tripRepository).should().findById(eq(tripId));
        then(tripRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("커서 기반으로 여행 목록을 조회할 때 빈 목록을 반환한다")
    void getUserTrips_EmptyList() {
        // given
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(tripRepository.findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<TripDto> result = tripService.getUserTrips(user, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getSize()).isEqualTo(0);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        then(tripRepository).should().findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("커서 기반으로 여행 목록을 조회할 때 정확히 limit만큼 결과가 있으면 hasNext는 false다")
    void getUserTrips_ExactlyLimit() {
        // given
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        List<Trip> trips = List.of(trip);

        given(tripRepository.findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(trips);
        given(tripMapper.toDto(any(Trip.class))).willReturn(tripDto);

        // when
        CursorPageResponseDto<TripDto> result = tripService.getUserTrips(user, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        then(tripRepository).should().findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("커서 기반으로 여행 목록을 조회할 때 커서와 after 값이 있는 경우 다음 페이지를 조회한다")
    void getUserTrips_WithCursorAndAfter() {
        // given
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = "2025-11-27T10:00:00";
        Long after = 1L;
        int limit = 2;

        Trip trip2 = Trip.createTrip(
                "프랑스 여행",
                LocalDate.of(2025, 12, 15),
                LocalDate.of(2025, 12, 25),
                "image2.jpg",
                Set.of(Country.FR)
        );
        ReflectionTestUtils.setField(trip2, "id", 2L);
        ReflectionTestUtils.setField(trip2, "createdAt", LocalDateTime.of(2025, 11, 28, 10, 0));

        Trip trip3 = Trip.createTrip(
                "스페인 여행",
                LocalDate.of(2025, 12, 20),
                LocalDate.of(2025, 12, 30),
                "image3.jpg",
                Set.of(Country.ES)
        );
        ReflectionTestUtils.setField(trip3, "id", 3L);
        ReflectionTestUtils.setField(trip3, "createdAt", LocalDateTime.of(2025, 11, 29, 10, 0));

        List<Trip> trips = List.of(trip2, trip3);

        TripDto tripDto2 = TripDto.builder()
                .id(2L)
                .title("프랑스 여행")
                .startDate(LocalDate.of(2025, 12, 15))
                .endDate(LocalDate.of(2025, 12, 25))
                .imageUrl("image2.jpg")
                .build();

        TripDto tripDto3 = TripDto.builder()
                .id(3L)
                .title("스페인 여행")
                .startDate(LocalDate.of(2025, 12, 20))
                .endDate(LocalDate.of(2025, 12, 30))
                .imageUrl("image3.jpg")
                .build();

        given(tripRepository.findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(trips);
        given(tripMapper.toDto(eq(trip2))).willReturn(tripDto2);
        given(tripMapper.toDto(eq(trip3))).willReturn(tripDto3);

        // when
        CursorPageResponseDto<TripDto> result = tripService.getUserTrips(user, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        then(tripRepository).should().findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(tripMapper).should().toDto(eq(trip2));
        then(tripMapper).should().toDto(eq(trip3));
    }

    @Test
    @DisplayName("커서 기반으로 여행 목록을 조회할 때 여러 개의 여행이 있고 hasNext가 false인 경우")
    void getUserTrips_MultipleTrips_NoNext() {
        // given
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        Trip trip2 = Trip.createTrip(
                "프랑스 여행",
                LocalDate.of(2025, 12, 15),
                LocalDate.of(2025, 12, 25),
                "image2.jpg",
                Set.of(Country.FR)
        );
        ReflectionTestUtils.setField(trip2, "id", 2L);
        ReflectionTestUtils.setField(trip2, "createdAt", LocalDateTime.of(2025, 11, 28, 10, 0));

        List<Trip> trips = List.of(trip, trip2);

        TripDto tripDto2 = TripDto.builder()
                .id(2L)
                .title("프랑스 여행")
                .startDate(LocalDate.of(2025, 12, 15))
                .endDate(LocalDate.of(2025, 12, 25))
                .imageUrl("image2.jpg")
                .build();

        given(tripRepository.findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(trips);
        given(tripMapper.toDto(eq(trip))).willReturn(tripDto);
        given(tripMapper.toDto(eq(trip2))).willReturn(tripDto2);

        // when
        CursorPageResponseDto<TripDto> result = tripService.getUserTrips(user, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
        then(tripRepository).should().findAllByUserCursor(eq("testuser"), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(tripMapper).should().toDto(eq(trip));
        then(tripMapper).should().toDto(eq(trip2));
    }
}
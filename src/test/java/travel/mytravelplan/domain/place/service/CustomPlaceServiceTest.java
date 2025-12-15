package travel.mytravelplan.domain.place.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.place.dto.CustomPlaceCreateRequestDto;
import travel.mytravelplan.domain.place.dto.CustomPlaceDto;
import travel.mytravelplan.domain.place.dto.CustomPlaceUpdateRequestDto;
import travel.mytravelplan.domain.place.entity.CustomPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.enums.PlaceType;
import travel.mytravelplan.domain.place.exception.CustomPlaceException;
import travel.mytravelplan.domain.place.mapper.CustomPlaceMapper;
import travel.mytravelplan.domain.place.repository.CustomPlaceRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("나만의 장소 서비스 테스트")
class CustomPlaceServiceTest extends ServiceTestSupport {

    @Mock
    private CustomPlaceRepository customPlaceRepository;

    @Mock
    private CustomPlaceMapper customPlaceMapper;

    @InjectMocks
    private CustomPlaceService customPlaceService;

    private User user;
    private CustomPlace customPlace;
    private CustomPlaceDto customPlaceDto;
    private CustomPlaceCreateRequestDto createRequestDto;
    private CustomPlaceUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        // User 설정
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);

        // CustomPlace 설정
        customPlace = CustomPlace.createCustomPlace(
                "테스트 장소",
                "서울시 강남구",
                "테스트 장소 설명",
                new BigDecimal("37.123456"),
                new BigDecimal("127.123456"),
                PlaceCategory.RESTAURANT,
                user
        );
        ReflectionTestUtils.setField(customPlace, "id", 1L);
        ReflectionTestUtils.setField(customPlace, "createdAt", LocalDateTime.now());

        // CustomPlaceDto 설정
        customPlaceDto = CustomPlaceDto.builder()
                .id(1L)
                .name("테스트 장소")
                .address("서울시 강남구")
                .description("테스트 장소 설명")
                .latitude(new BigDecimal("37.123456"))
                .longitude(new BigDecimal("127.123456"))
                .build();

        // 생성 요청 DTO
        createRequestDto = CustomPlaceCreateRequestDto.builder()
                .placeType(PlaceType.CUSTOM)
                .name("새로운 장소")
                .address("서울시 서초구")
                .description("새로운 장소 설명")
                .latitude(new BigDecimal("37.111111"))
                .longitude(new BigDecimal("127.111111"))
                .category(PlaceCategory.CAFE)
                .build();

        // 수정 요청 DTO
        updateRequestDto = CustomPlaceUpdateRequestDto.builder()
                .placeType(PlaceType.CUSTOM)
                .name("수정된 장소")
                .address("서울시 송파구")
                .description("수정된 장소 설명")
                .latitude(new BigDecimal("37.222222"))
                .longitude(new BigDecimal("127.222222"))
                .category(PlaceCategory.ATTRACTION)
                .build();
    }

    @Test
    @DisplayName("나만의 장소 생성 성공")
    void createCustomPlace_Success() {
        // given
        given(customPlaceRepository.save(any(CustomPlace.class))).willReturn(customPlace);
        given(customPlaceMapper.toDto(any(CustomPlace.class))).willReturn(customPlaceDto);

        // when
        CustomPlaceDto result = customPlaceService.createCustomPlace(user, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(customPlaceDto);

        then(customPlaceRepository).should().save(any(CustomPlace.class));
        then(customPlaceMapper).should().toDto(any(CustomPlace.class));
    }

    @Test
    @DisplayName("나만의 장소 단건 조회 성공")
    void getCustomPlace_Success() {
        // given
        Long customPlaceId = 1L;
        given(customPlaceRepository.findById(eq(customPlaceId))).willReturn(Optional.of(customPlace));
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(customPlaceDto);

        // when
        CustomPlaceDto result = customPlaceService.getCustomPlace(customPlaceId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(customPlaceDto);

        then(customPlaceRepository).should().findById(eq(customPlaceId));
        then(customPlaceMapper).should().toDto(eq(customPlace));
    }

    @Test
    @DisplayName("나만의 장소 단건 조회 실패 - 존재하지 않는 장소")
    void getCustomPlace_NotFound() {
        // given
        Long customPlaceId = 999L;
        given(customPlaceRepository.findById(eq(customPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customPlaceService.getCustomPlace(customPlaceId))
                .isInstanceOf(CustomPlaceException.class);

        then(customPlaceRepository).should().findById(eq(customPlaceId));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - 다음 페이지 있음")
    void getCustomPlaces_Success_WithNext() {
        // given
        CustomPlace customPlace2 = CustomPlace.createCustomPlace(
                "테스트 장소2",
                "서울시 서초구",
                "테스트 장소 설명2",
                new BigDecimal("37.222222"),
                new BigDecimal("127.222222"),
                PlaceCategory.CAFE,
                user
        );
        ReflectionTestUtils.setField(customPlace2, "id", 2L);
        ReflectionTestUtils.setField(customPlace2, "createdAt", LocalDateTime.now().minusHours(1));

        CustomPlace customPlace3 = CustomPlace.createCustomPlace(
                "테스트 장소3",
                "서울시 송파구",
                "테스트 장소 설명3",
                new BigDecimal("37.333333"),
                new BigDecimal("127.333333"),
                PlaceCategory.ATTRACTION,
                user
        );
        ReflectionTestUtils.setField(customPlace3, "id", 3L);
        ReflectionTestUtils.setField(customPlace3, "createdAt", LocalDateTime.now().minusHours(2));

        List<CustomPlace> customPlaces = Arrays.asList(customPlace, customPlace2, customPlace3);

        CustomPlaceDto customPlaceDto2 = CustomPlaceDto.builder()
                .id(2L)
                .name("테스트 장소2")
                .address("서울시 서초구")
                .description("테스트 장소 설명2")
                .latitude(new BigDecimal("37.222222"))
                .longitude(new BigDecimal("127.222222"))
                .build();

        String keyword = "테스트";
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(3)))
                .willReturn(customPlaces);
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(customPlaceDto);
        given(customPlaceMapper.toDto(eq(customPlace2))).willReturn(customPlaceDto2);

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(3));
        then(customPlaceMapper).should().toDto(eq(customPlace));
        then(customPlaceMapper).should().toDto(eq(customPlace2));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - 다음 페이지 없음")
    void getCustomPlaces_Success_WithoutNext() {
        // given
        CustomPlace customPlace2 = CustomPlace.createCustomPlace(
                "테스트 장소2",
                "서울시 서초구",
                "테스트 장소 설명2",
                new BigDecimal("37.222222"),
                new BigDecimal("127.222222"),
                PlaceCategory.CAFE,
                user
        );
        ReflectionTestUtils.setField(customPlace2, "id", 2L);
        ReflectionTestUtils.setField(customPlace2, "createdAt", LocalDateTime.now().minusHours(1));

        List<CustomPlace> customPlaces = Arrays.asList(customPlace, customPlace2);

        CustomPlaceDto customPlaceDto2 = CustomPlaceDto.builder()
                .id(2L)
                .name("테스트 장소2")
                .address("서울시 서초구")
                .description("테스트 장소 설명2")
                .latitude(new BigDecimal("37.222222"))
                .longitude(new BigDecimal("127.222222"))
                .build();

        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11)))
                .willReturn(customPlaces);
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(customPlaceDto);
        given(customPlaceMapper.toDto(eq(customPlace2))).willReturn(customPlaceDto2);

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11));
        then(customPlaceMapper).should().toDto(eq(customPlace));
        then(customPlaceMapper).should().toDto(eq(customPlace2));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - 빈 목록")
    void getCustomPlaces_Success_EmptyList() {
        // given
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getSize()).isEqualTo(0);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - cursor와 after 파라미터 사용")
    void getCustomPlaces_Success_WithCursorAndAfter() {
        // given
        CustomPlace customPlace2 = CustomPlace.createCustomPlace(
                "테스트 장소2",
                "서울시 서초구",
                "테스트 장소 설명2",
                new BigDecimal("37.222222"),
                new BigDecimal("127.222222"),
                PlaceCategory.CAFE,
                user
        );
        ReflectionTestUtils.setField(customPlace2, "id", 2L);
        ReflectionTestUtils.setField(customPlace2, "createdAt", LocalDateTime.now().minusHours(1));

        List<CustomPlace> customPlaces = List.of(customPlace2);

        CustomPlaceDto customPlaceDto2 = CustomPlaceDto.builder()
                .id(2L)
                .name("테스트 장소2")
                .address("서울시 서초구")
                .description("테스트 장소 설명2")
                .latitude(new BigDecimal("37.222222"))
                .longitude(new BigDecimal("127.222222"))
                .build();

        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = "2024-12-01T12:00:00";
        Long after = 1L;
        int limit = 10;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11)))
                .willReturn(customPlaces);
        given(customPlaceMapper.toDto(eq(customPlace2))).willReturn(customPlaceDto2);

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11));
        then(customPlaceMapper).should().toDto(eq(customPlace2));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - keyword 사용")
    void getCustomPlaces_Success_WithKeyword() {
        // given
        List<CustomPlace> customPlaces = List.of(customPlace);

        String keyword = "테스트";
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11)))
                .willReturn(customPlaces);
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(customPlaceDto);

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11));
        then(customPlaceMapper).should().toDto(eq(customPlace));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - 1개 결과")
    void getCustomPlaces_Success_SingleResult() {
        // given
        List<CustomPlace> customPlaces = List.of(customPlace);

        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11)))
                .willReturn(customPlaces);
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(customPlaceDto);

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11));
        then(customPlaceMapper).should().toDto(eq(customPlace));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - direction이 asc인 경우")
    void getCustomPlaces_Success_DirectionAsc() {
        // given
        CustomPlace customPlace2 = CustomPlace.createCustomPlace(
                "테스트 장소2",
                "서울시 서초구",
                "테스트 장소 설명2",
                new BigDecimal("37.222222"),
                new BigDecimal("127.222222"),
                PlaceCategory.CAFE,
                user
        );
        ReflectionTestUtils.setField(customPlace2, "id", 2L);
        ReflectionTestUtils.setField(customPlace2, "createdAt", LocalDateTime.now().plusHours(1));

        List<CustomPlace> customPlaces = Arrays.asList(customPlace, customPlace2);

        CustomPlaceDto customPlaceDto2 = CustomPlaceDto.builder()
                .id(2L)
                .name("테스트 장소2")
                .address("서울시 서초구")
                .description("테스트 장소 설명2")
                .latitude(new BigDecimal("37.222222"))
                .longitude(new BigDecimal("127.222222"))
                .build();

        String keyword = null;
        String orderBy = "createdAt";
        String direction = "asc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11)))
                .willReturn(customPlaces);
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(customPlaceDto);
        given(customPlaceMapper.toDto(eq(customPlace2))).willReturn(customPlaceDto2);

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getHasNext()).isFalse();

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(11));
        then(customPlaceMapper).should().toDto(eq(customPlace));
        then(customPlaceMapper).should().toDto(eq(customPlace2));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - keyword와 hasNext true 조합")
    void getCustomPlaces_Success_WithKeywordAndHasNext() {
        // given
        CustomPlace customPlace2 = CustomPlace.createCustomPlace(
                "테스트 장소2",
                "서울시 서초구",
                "테스트 장소 설명2",
                new BigDecimal("37.222222"),
                new BigDecimal("127.222222"),
                PlaceCategory.CAFE,
                user
        );
        ReflectionTestUtils.setField(customPlace2, "id", 2L);
        ReflectionTestUtils.setField(customPlace2, "createdAt", LocalDateTime.now().minusHours(1));

        CustomPlace customPlace3 = CustomPlace.createCustomPlace(
                "테스트 장소3",
                "서울시 송파구",
                "테스트 장소 설명3",
                new BigDecimal("37.333333"),
                new BigDecimal("127.333333"),
                PlaceCategory.ATTRACTION,
                user
        );
        ReflectionTestUtils.setField(customPlace3, "id", 3L);
        ReflectionTestUtils.setField(customPlace3, "createdAt", LocalDateTime.now().minusHours(2));

        List<CustomPlace> customPlaces = Arrays.asList(customPlace, customPlace2, customPlace3);

        CustomPlaceDto customPlaceDto2 = CustomPlaceDto.builder()
                .id(2L)
                .name("테스트 장소2")
                .address("서울시 서초구")
                .description("테스트 장소 설명2")
                .latitude(new BigDecimal("37.222222"))
                .longitude(new BigDecimal("127.222222"))
                .build();

        String keyword = "테스트";
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(3)))
                .willReturn(customPlaces);
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(customPlaceDto);
        given(customPlaceMapper.toDto(eq(customPlace2))).willReturn(customPlaceDto2);

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(3));
        then(customPlaceMapper).should().toDto(eq(customPlace));
        then(customPlaceMapper).should().toDto(eq(customPlace2));
    }

    @Test
    @DisplayName("나만의 장소 목록 조회 성공 - limit 1인 경우")
    void getCustomPlaces_Success_LimitOne() {
        // given
        CustomPlace customPlace2 = CustomPlace.createCustomPlace(
                "테스트 장소2",
                "서울시 서초구",
                "테스트 장소 설명2",
                new BigDecimal("37.222222"),
                new BigDecimal("127.222222"),
                PlaceCategory.CAFE,
                user
        );
        ReflectionTestUtils.setField(customPlace2, "id", 2L);
        ReflectionTestUtils.setField(customPlace2, "createdAt", LocalDateTime.now().minusHours(1));

        List<CustomPlace> customPlaces = Arrays.asList(customPlace, customPlace2);

        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        given(customPlaceRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(2)))
                .willReturn(customPlaces);
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(customPlaceDto);

        // when
        CursorPageResponseDto<CustomPlaceDto> result = customPlaceService.getCustomPlaces(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isEqualTo(1L);

        then(customPlaceRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(2));
        then(customPlaceMapper).should().toDto(eq(customPlace));
    }

    @Test
    @DisplayName("나만의 장소 수정 성공")
    void updateCustomPlace_Success() {
        // given
        Long customPlaceId = 1L;
        CustomPlaceDto updatedDto = CustomPlaceDto.builder()
                .id(1L)
                .name("수정된 장소")
                .address("서울시 송파구")
                .description("수정된 장소 설명")
                .latitude(new BigDecimal("37.222222"))
                .longitude(new BigDecimal("127.222222"))
                .build();

        given(customPlaceRepository.findById(eq(customPlaceId))).willReturn(Optional.of(customPlace));
        given(customPlaceMapper.toDto(eq(customPlace))).willReturn(updatedDto);

        // when
        CustomPlaceDto result = customPlaceService.updateCustomPlace(customPlaceId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedDto);

        then(customPlaceRepository).should().findById(eq(customPlaceId));
        then(customPlaceMapper).should().toDto(eq(customPlace));
    }

    @Test
    @DisplayName("나만의 장소 수정 실패 - 존재하지 않는 장소")
    void updateCustomPlace_NotFound() {
        // given
        Long customPlaceId = 999L;
        given(customPlaceRepository.findById(eq(customPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customPlaceService.updateCustomPlace(customPlaceId, updateRequestDto))
                .isInstanceOf(CustomPlaceException.class);

        then(customPlaceRepository).should().findById(eq(customPlaceId));
    }

    @Test
    @DisplayName("나만의 장소 삭제 성공")
    void deleteCustomPlace_Success() {
        // given
        Long customPlaceId = 1L;
        given(customPlaceRepository.findById(eq(customPlaceId))).willReturn(Optional.of(customPlace));

        // when
        customPlaceService.deleteCustomPlace(customPlaceId);

        // then
        then(customPlaceRepository).should().findById(eq(customPlaceId));
        then(customPlaceRepository).should().delete(eq(customPlace));
    }

    @Test
    @DisplayName("나만의 장소 삭제 실패 - 존재하지 않는 장소")
    void deleteCustomPlace_NotFound() {
        // given
        Long customPlaceId = 999L;
        given(customPlaceRepository.findById(eq(customPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customPlaceService.deleteCustomPlace(customPlaceId))
                .isInstanceOf(CustomPlaceException.class);

        then(customPlaceRepository).should().findById(eq(customPlaceId));
    }
}
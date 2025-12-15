package travel.mytravelplan.domain.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.exception.TripPlaceException;
import travel.mytravelplan.domain.place.repository.TripPlaceRepository;
import travel.mytravelplan.domain.review.dto.TripPlaceReviewCreateRequestDto;
import travel.mytravelplan.domain.review.dto.TripPlaceReviewDto;
import travel.mytravelplan.domain.review.dto.TripPlaceReviewLikeDto;
import travel.mytravelplan.domain.review.dto.TripPlaceReviewUpdateRequestDto;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.review.entity.TripPlaceReviewLike;
import travel.mytravelplan.domain.review.exception.TripPlaceReviewException;
import travel.mytravelplan.domain.review.mapper.TripPlaceReviewLikeMapper;
import travel.mytravelplan.domain.review.mapper.TripPlaceReviewMapper;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewLikeRepository;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.TripPlaceErrorCode;
import travel.mytravelplan.global.error.code.TripPlaceReviewErrorCode;
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

@DisplayName("여행 장소 리뷰 서비스 테스트")
class TripPlaceReviewServiceTest extends ServiceTestSupport {

    @InjectMocks
    private TripPlaceReviewService tripPlaceReviewService;

    @Mock
    private TripPlaceRepository tripPlaceRepository;

    @Mock
    private TripPlaceReviewRepository tripPlaceReviewRepository;

    @Mock
    private TripPlaceReviewLikeRepository tripPlaceReviewLikeRepository;

    @Mock
    private TripPlaceReviewMapper tripPlaceReviewMapper;

    @Mock
    private TripPlaceReviewLikeMapper tripPlaceReviewLikeMapper;

    private User user;
    private TripPlace tripPlace;
    private TripPlaceReview tripPlaceReview;

    @BeforeEach
    void setUp() {
        user = User.createUser(
                "testUser",
                "password123",
                "test@example.com",
                SocialType.KAKAO,
                "socialId123",
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        tripPlace = TripPlace.createTripPlace(
                "Test Place",
                "Test Address",
                "Test Description",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                PlaceCategory.ATTRACTION,
                "http://example.com"
        );

        tripPlaceReview = TripPlaceReview.createTripPlaceReview(
                user,
                tripPlace,
                new BigDecimal("4.5"),
                "Great place to visit!"
        );
    }

    @Test
    @DisplayName("여행 장소 리뷰 생성 - 성공")
    void createTripPlaceReview_Success() {
        // given
        Long tripPlaceId = 1L;
        TripPlaceReviewCreateRequestDto requestDto = TripPlaceReviewCreateRequestDto.builder()
                .content("Great place!")
                .rating(new BigDecimal("4.5"))
                .build();

        TripPlaceReviewDto expectedDto = TripPlaceReviewDto.builder()
                .id(1L)
                .content("Great place!")
                .rating(new BigDecimal("4.5"))
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewMapper.toDto(any(TripPlaceReview.class), eq(user))).willReturn(expectedDto);

        // when
        TripPlaceReviewDto result = tripPlaceReviewService.createTripPlaceReview(user, tripPlaceId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Great place!");
        assertThat(result.getRating()).isEqualTo(new BigDecimal("4.5"));
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().save(any(TripPlaceReview.class));
        then(tripPlaceReviewMapper).should().toDto(any(TripPlaceReview.class), eq(user));
    }

    @Test
    @DisplayName("여행 장소 리뷰 생성 - 여행 장소를 찾을 수 없음")
    void createTripPlaceReview_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        TripPlaceReviewCreateRequestDto requestDto = TripPlaceReviewCreateRequestDto.builder()
                .content("Great place!")
                .rating(new BigDecimal("4.5"))
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.createTripPlaceReview(user, tripPlaceId, requestDto))
                .isInstanceOf(TripPlaceException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND);

        then(tripPlaceReviewRepository).should(never()).save(any(TripPlaceReview.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 조회 - 성공")
    void getTripPlaceReview_Success() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        TripPlaceReviewDto expectedDto = TripPlaceReviewDto.builder()
                .id(reviewId)
                .content("Great place!")
                .rating(new BigDecimal("4.5"))
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewMapper.toDto(eq(tripPlaceReview), eq(user))).willReturn(expectedDto);

        // when
        TripPlaceReviewDto result = tripPlaceReviewService.getTripPlaceReview(user, tripPlaceId, reviewId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reviewId);
        assertThat(result.getContent()).isEqualTo("Great place!");
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(reviewId));
        then(tripPlaceReviewMapper).should().toDto(eq(tripPlaceReview), eq(user));
    }

    @Test
    @DisplayName("여행 장소 리뷰 조회 - 리뷰를 찾을 수 없음")
    void getTripPlaceReview_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.getTripPlaceReview(user, tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceReviewException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("여행 장소 리뷰 목록 조회 - 성공")
    void getTripPlaceReviews_Success() {
        // given
        Long tripPlaceId = 1L;
        String keyword = null;
        boolean imgOnly = false;
        BigDecimal rating = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<TripPlaceReview> reviews = List.of(tripPlaceReview);
        TripPlaceReviewDto reviewDto = TripPlaceReviewDto.builder()
                .id(1L)
                .content("Great place!")
                .rating(new BigDecimal("4.5"))
                .build();

        given(tripPlaceReviewRepository.findAllByCursor(
                eq(tripPlaceId), eq(keyword), eq(imgOnly), eq(rating),
                eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)
        )).willReturn(reviews);
        given(tripPlaceReviewMapper.toDto(any(TripPlaceReview.class), eq(user))).willReturn(reviewDto);

        // when
        CursorPageResponseDto<TripPlaceReviewDto> result = tripPlaceReviewService.getTripPlaceReviews(
                user, tripPlaceId, keyword, imgOnly, rating, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(1);
        then(tripPlaceReviewRepository).should().findAllByCursor(
                eq(tripPlaceId), eq(keyword), eq(imgOnly), eq(rating),
                eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)
        );
    }

    @Test
    @DisplayName("여행 장소 리뷰 수정 - 성공")
    void updateTripPlaceReview_Success() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        TripPlaceReviewUpdateRequestDto requestDto = TripPlaceReviewUpdateRequestDto.builder()
                .content("Updated content")
                .rating(new BigDecimal("5.0"))
                .build();

        TripPlaceReviewDto expectedDto = TripPlaceReviewDto.builder()
                .id(reviewId)
                .content("Updated content")
                .rating(new BigDecimal("5.0"))
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewMapper.toDto(eq(tripPlaceReview), eq(user))).willReturn(expectedDto);

        // when
        TripPlaceReviewDto result = tripPlaceReviewService.updateTripPlaceReview(user, tripPlaceId, reviewId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Updated content");
        assertThat(result.getRating()).isEqualTo(new BigDecimal("5.0"));
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(reviewId));
        then(tripPlaceReviewMapper).should().toDto(eq(tripPlaceReview), eq(user));
    }

    @Test
    @DisplayName("여행 장소 리뷰 삭제 - 성공")
    void deleteTripPlaceReview_Success() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(tripPlaceReview));

        // when
        tripPlaceReviewService.deleteTripPlaceReview(tripPlaceId, reviewId);

        // then
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(reviewId));
        then(tripPlaceReviewRepository).should().delete(eq(tripPlaceReview));
    }

    @Test
    @DisplayName("여행 장소 리뷰 삭제 - 여행 장소를 찾을 수 없음")
    void deleteTripPlaceReview_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long reviewId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.deleteTripPlaceReview(tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND);

        then(tripPlaceReviewRepository).should(never()).delete(any(TripPlaceReview.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 좋아요 - 좋아요 추가 성공")
    void likeTripPlaceReview_AddLike_Success() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        TripPlaceReviewLikeDto expectedDto = TripPlaceReviewLikeDto.builder()
                .tripPlaceReviewId(reviewId)
                .userId(1L)
                .liked(true)
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewLikeRepository.findByTripPlaceReviewAndUser(eq(tripPlaceReview), eq(user)))
                .willReturn(Optional.empty());
        given(tripPlaceReviewLikeMapper.toDto(any(TripPlaceReviewLike.class), eq(true)))
                .willReturn(expectedDto);

        // when
        TripPlaceReviewLikeDto result = tripPlaceReviewService.likeTripPlaceReview(user, tripPlaceId, reviewId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isLiked()).isTrue();
        then(tripPlaceReviewLikeRepository).should().save(any(TripPlaceReviewLike.class));
        then(tripPlaceReviewLikeRepository).should(never()).delete(any(TripPlaceReviewLike.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 좋아요 - 좋아요 취소 성공")
    void likeTripPlaceReview_RemoveLike_Success() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        TripPlaceReviewLike reviewLike = TripPlaceReviewLike.createTripPlaceReviewLike(tripPlaceReview, user);
        TripPlaceReviewLikeDto expectedDto = TripPlaceReviewLikeDto.builder()
                .tripPlaceReviewId(reviewId)
                .userId(1L)
                .liked(false)
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewLikeRepository.findByTripPlaceReviewAndUser(eq(tripPlaceReview), eq(user)))
                .willReturn(Optional.of(reviewLike));
        given(tripPlaceReviewLikeMapper.toDto(eq(reviewLike), eq(false)))
                .willReturn(expectedDto);

        // when
        TripPlaceReviewLikeDto result = tripPlaceReviewService.likeTripPlaceReview(user, tripPlaceId, reviewId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isLiked()).isFalse();
        then(tripPlaceReviewLikeRepository).should().delete(eq(reviewLike));
        then(tripPlaceReviewLikeRepository).should(never()).save(any(TripPlaceReviewLike.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 조회 - 리뷰가 해당 여행 장소에 속하지 않음")
    void getTripPlaceReview_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        TripPlace anotherTripPlace = TripPlace.createTripPlace(
                "Another Place",
                "Another Address",
                "Another Description",
                new BigDecimal("35.1796"),
                new BigDecimal("129.0756"),
                PlaceCategory.RESTAURANT,
                "http://another.com"
        );

        TripPlaceReview reviewFromAnotherPlace = TripPlaceReview.createTripPlaceReview(
                user,
                anotherTripPlace,
                new BigDecimal("3.0"),
                "Different place review"
        );

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(reviewFromAnotherPlace));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.getTripPlaceReview(user, tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceReviewException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_BELONG_TO_TRIP_PLACE);
    }

    @Test
    @DisplayName("여행 장소 리뷰 수정 - 여행 장소를 찾을 수 없음")
    void updateTripPlaceReview_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long reviewId = 1L;

        TripPlaceReviewUpdateRequestDto requestDto = TripPlaceReviewUpdateRequestDto.builder()
                .content("Updated content")
                .rating(new BigDecimal("5.0"))
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.updateTripPlaceReview(user, tripPlaceId, reviewId, requestDto))
                .isInstanceOf(TripPlaceException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND);

        then(tripPlaceReviewRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("여행 장소 리뷰 수정 - 리뷰를 찾을 수 없음")
    void updateTripPlaceReview_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 999L;

        TripPlaceReviewUpdateRequestDto requestDto = TripPlaceReviewUpdateRequestDto.builder()
                .content("Updated content")
                .rating(new BigDecimal("5.0"))
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.updateTripPlaceReview(user, tripPlaceId, reviewId, requestDto))
                .isInstanceOf(TripPlaceReviewException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("여행 장소 리뷰 수정 - 리뷰가 해당 여행 장소에 속하지 않음")
    void updateTripPlaceReview_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        TripPlace anotherTripPlace = TripPlace.createTripPlace(
                "Another Place",
                "Another Address",
                "Another Description",
                new BigDecimal("35.1796"),
                new BigDecimal("129.0756"),
                PlaceCategory.RESTAURANT,
                "http://another.com"
        );

        TripPlaceReview reviewFromAnotherPlace = TripPlaceReview.createTripPlaceReview(
                user,
                anotherTripPlace,
                new BigDecimal("3.0"),
                "Different place review"
        );

        TripPlaceReviewUpdateRequestDto requestDto = TripPlaceReviewUpdateRequestDto.builder()
                .content("Updated content")
                .rating(new BigDecimal("5.0"))
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(reviewFromAnotherPlace));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.updateTripPlaceReview(user, tripPlaceId, reviewId, requestDto))
                .isInstanceOf(TripPlaceReviewException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_BELONG_TO_TRIP_PLACE);
    }

    @Test
    @DisplayName("여행 장소 리뷰 삭제 - 리뷰를 찾을 수 없음")
    void deleteTripPlaceReview_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.deleteTripPlaceReview(tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceReviewException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND);

        then(tripPlaceReviewRepository).should(never()).delete(any(TripPlaceReview.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 삭제 - 리뷰가 해당 여행 장소에 속하지 않음")
    void deleteTripPlaceReview_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        TripPlace anotherTripPlace = TripPlace.createTripPlace(
                "Another Place",
                "Another Address",
                "Another Description",
                new BigDecimal("35.1796"),
                new BigDecimal("129.0756"),
                PlaceCategory.RESTAURANT,
                "http://another.com"
        );

        TripPlaceReview reviewFromAnotherPlace = TripPlaceReview.createTripPlaceReview(
                user,
                anotherTripPlace,
                new BigDecimal("3.0"),
                "Different place review"
        );

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(reviewFromAnotherPlace));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.deleteTripPlaceReview(tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceReviewException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_BELONG_TO_TRIP_PLACE);

        then(tripPlaceReviewRepository).should(never()).delete(any(TripPlaceReview.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 목록 조회 - 다음 페이지가 있는 경우 (hasNext = true)")
    void getTripPlaceReviews_HasNext() {
        // given
        Long tripPlaceId = 1L;
        String keyword = null;
        boolean imgOnly = false;
        BigDecimal rating = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        TripPlaceReview review1 = TripPlaceReview.createTripPlaceReview(
                user, tripPlace, new BigDecimal("4.5"), "Review 1"
        );
        ReflectionTestUtils.setField(review1, "id", 1L);
        ReflectionTestUtils.setField(review1, "createdAt", LocalDateTime.now().minusDays(3));

        TripPlaceReview review2 = TripPlaceReview.createTripPlaceReview(
                user, tripPlace, new BigDecimal("4.0"), "Review 2"
        );
        ReflectionTestUtils.setField(review2, "id", 2L);
        ReflectionTestUtils.setField(review2, "createdAt", LocalDateTime.now().minusDays(2));

        TripPlaceReview review3 = TripPlaceReview.createTripPlaceReview(
                user, tripPlace, new BigDecimal("3.5"), "Review 3"
        );
        ReflectionTestUtils.setField(review3, "id", 3L);
        ReflectionTestUtils.setField(review3, "createdAt", LocalDateTime.now().minusDays(1));

        List<TripPlaceReview> reviews = List.of(review1, review2, review3);

        TripPlaceReviewDto reviewDto1 = TripPlaceReviewDto.builder()
                .id(1L)
                .content("Review 1")
                .rating(new BigDecimal("4.5"))
                .build();

        TripPlaceReviewDto reviewDto2 = TripPlaceReviewDto.builder()
                .id(2L)
                .content("Review 2")
                .rating(new BigDecimal("4.0"))
                .build();

        given(tripPlaceReviewRepository.findAllByCursor(
                eq(tripPlaceId), eq(keyword), eq(imgOnly), eq(rating),
                eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)
        )).willReturn(reviews);
        given(tripPlaceReviewMapper.toDto(eq(review1), eq(user))).willReturn(reviewDto1);
        given(tripPlaceReviewMapper.toDto(eq(review2), eq(user))).willReturn(reviewDto2);

        // when
        CursorPageResponseDto<TripPlaceReviewDto> result = tripPlaceReviewService.getTripPlaceReviews(
                user, tripPlaceId, keyword, imgOnly, rating, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();
    }

    @Test
    @DisplayName("여행 장소 리뷰 목록 조회 - rating 기준 정렬")
    void getTripPlaceReviews_OrderByRating() {
        // given
        Long tripPlaceId = 1L;
        String keyword = null;
        boolean imgOnly = false;
        BigDecimal rating = null;
        String orderBy = "rating";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        TripPlaceReview review1 = TripPlaceReview.createTripPlaceReview(
                user, tripPlace, new BigDecimal("4.5"), "Review 1"
        );
        ReflectionTestUtils.setField(review1, "id", 1L);

        TripPlaceReview review2 = TripPlaceReview.createTripPlaceReview(
                user, tripPlace, new BigDecimal("4.0"), "Review 2"
        );
        ReflectionTestUtils.setField(review2, "id", 2L);

        TripPlaceReview review3 = TripPlaceReview.createTripPlaceReview(
                user, tripPlace, new BigDecimal("3.5"), "Review 3"
        );
        ReflectionTestUtils.setField(review3, "id", 3L);

        List<TripPlaceReview> reviews = List.of(review1, review2, review3);

        TripPlaceReviewDto reviewDto1 = TripPlaceReviewDto.builder()
                .id(1L)
                .content("Review 1")
                .rating(new BigDecimal("4.5"))
                .build();

        TripPlaceReviewDto reviewDto2 = TripPlaceReviewDto.builder()
                .id(2L)
                .content("Review 2")
                .rating(new BigDecimal("4.0"))
                .build();

        given(tripPlaceReviewRepository.findAllByCursor(
                eq(tripPlaceId), eq(keyword), eq(imgOnly), eq(rating),
                eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)
        )).willReturn(reviews);
        given(tripPlaceReviewMapper.toDto(eq(review1), eq(user))).willReturn(reviewDto1);
        given(tripPlaceReviewMapper.toDto(eq(review2), eq(user))).willReturn(reviewDto2);

        // when
        CursorPageResponseDto<TripPlaceReviewDto> result = tripPlaceReviewService.getTripPlaceReviews(
                user, tripPlaceId, keyword, imgOnly, rating, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();
    }

    @Test
    @DisplayName("여행 장소 리뷰 좋아요 - 여행 장소를 찾을 수 없음")
    void likeTripPlaceReview_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long reviewId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.likeTripPlaceReview(user, tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND);

        then(tripPlaceReviewLikeRepository).should(never()).save(any(TripPlaceReviewLike.class));
        then(tripPlaceReviewLikeRepository).should(never()).delete(any(TripPlaceReviewLike.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 좋아요 - 리뷰를 찾을 수 없음")
    void likeTripPlaceReview_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.likeTripPlaceReview(user, tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceReviewException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND);

        then(tripPlaceReviewLikeRepository).should(never()).save(any(TripPlaceReviewLike.class));
        then(tripPlaceReviewLikeRepository).should(never()).delete(any(TripPlaceReviewLike.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 좋아요 - 리뷰가 해당 여행 장소에 속하지 않음")
    void likeTripPlaceReview_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long reviewId = 1L;

        TripPlace anotherTripPlace = TripPlace.createTripPlace(
                "Another Place",
                "Another Address",
                "Another Description",
                new BigDecimal("35.1796"),
                new BigDecimal("129.0756"),
                PlaceCategory.RESTAURANT,
                "http://another.com"
        );

        TripPlaceReview reviewFromAnotherPlace = TripPlaceReview.createTripPlaceReview(
                user,
                anotherTripPlace,
                new BigDecimal("3.0"),
                "Different place review"
        );

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(reviewId))).willReturn(Optional.of(reviewFromAnotherPlace));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.likeTripPlaceReview(user, tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceReviewException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_BELONG_TO_TRIP_PLACE);

        then(tripPlaceReviewLikeRepository).should(never()).save(any(TripPlaceReviewLike.class));
        then(tripPlaceReviewLikeRepository).should(never()).delete(any(TripPlaceReviewLike.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 목록 조회 - 키워드와 이미지 필터 적용")
    void getTripPlaceReviews_WithKeywordAndImgOnly() {
        // given
        Long tripPlaceId = 1L;
        String keyword = "great";
        boolean imgOnly = true;
        BigDecimal rating = new BigDecimal("4.0");
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<TripPlaceReview> reviews = List.of(tripPlaceReview);
        TripPlaceReviewDto reviewDto = TripPlaceReviewDto.builder()
                .id(1L)
                .content("Great place!")
                .rating(new BigDecimal("4.5"))
                .build();

        given(tripPlaceReviewRepository.findAllByCursor(
                eq(tripPlaceId), eq(keyword), eq(imgOnly), eq(rating),
                eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)
        )).willReturn(reviews);
        given(tripPlaceReviewMapper.toDto(any(TripPlaceReview.class), eq(user))).willReturn(reviewDto);

        // when
        CursorPageResponseDto<TripPlaceReviewDto> result = tripPlaceReviewService.getTripPlaceReviews(
                user, tripPlaceId, keyword, imgOnly, rating, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();
        then(tripPlaceReviewRepository).should().findAllByCursor(
                eq(tripPlaceId), eq(keyword), eq(imgOnly), eq(rating),
                eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)
        );
    }

    @Test
    @DisplayName("여행 장소 리뷰 목록 조회 - 빈 리스트 반환")
    void getTripPlaceReviews_EmptyList() {
        // given
        Long tripPlaceId = 1L;
        String keyword = null;
        boolean imgOnly = false;
        BigDecimal rating = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<TripPlaceReview> reviews = List.of();

        given(tripPlaceReviewRepository.findAllByCursor(
                eq(tripPlaceId), eq(keyword), eq(imgOnly), eq(rating),
                eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)
        )).willReturn(reviews);

        // when
        CursorPageResponseDto<TripPlaceReviewDto> result = tripPlaceReviewService.getTripPlaceReviews(
                user, tripPlaceId, keyword, imgOnly, rating, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(0);
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
    }

    @Test
    @DisplayName("여행 장소 리뷰 조회 - 여행 장소를 찾을 수 없음")
    void getTripPlaceReview_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long reviewId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewService.getTripPlaceReview(user, tripPlaceId, reviewId))
                .isInstanceOf(TripPlaceException.class)
                .hasFieldOrPropertyWithValue("errorCode", TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND);

        then(tripPlaceReviewRepository).should(never()).findById(any());
    }
}


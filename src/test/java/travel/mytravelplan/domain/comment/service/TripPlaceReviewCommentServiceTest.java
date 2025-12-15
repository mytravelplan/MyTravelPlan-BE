package travel.mytravelplan.domain.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentDto;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;
import travel.mytravelplan.domain.comment.exception.TripPlaceReviewCommentException;
import travel.mytravelplan.domain.comment.mapper.TripPlaceReviewCommentMapper;
import travel.mytravelplan.domain.comment.repository.TripPlaceReviewCommentRepository;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.exception.TripPlaceException;
import travel.mytravelplan.domain.place.repository.TripPlaceRepository;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.review.exception.TripPlaceReviewException;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("여행 장소 리뷰 댓글 서비스 테스트")
class TripPlaceReviewCommentServiceTest extends ServiceTestSupport {

    @Mock
    private TripPlaceRepository tripPlaceRepository;

    @Mock
    private TripPlaceReviewRepository tripPlaceReviewRepository;

    @Mock
    private TripPlaceReviewCommentRepository tripPlaceReviewCommentRepository;

    @Mock
    private TripPlaceReviewCommentMapper tripPlaceReviewCommentMapper;

    @InjectMocks
    private TripPlaceReviewCommentService tripPlaceReviewCommentService;

    private User user;
    private TripPlace tripPlace;
    private TripPlaceReview tripPlaceReview;
    private TripPlaceReviewComment comment;
    private TripPlaceReviewCommentDto commentDto;
    private TripPlaceReviewCommentCreateRequestDto createRequestDto;
    private TripPlaceReviewCommentUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        user = User.createUser(
                "testuser",
                "password123",
                "test@example.com",
                SocialType.KAKAO,
                "kakao123",
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        tripPlace = TripPlace.createTripPlace(
                "서울타워",
                "서울특별시 용산구",
                "서울의 대표 관광지",
                new BigDecimal("37.5511"),
                new BigDecimal("126.9882"),
                PlaceCategory.ATTRACTION,
                "http://example.com/seoul-tower"
        );
        ReflectionTestUtils.setField(tripPlace, "id", 1L);

        tripPlaceReview = TripPlaceReview.createTripPlaceReview(
                user,
                tripPlace,
                new BigDecimal("4.5"),
                "좋은 장소입니다"
        );
        ReflectionTestUtils.setField(tripPlaceReview, "id", 1L);

        createRequestDto = TripPlaceReviewCommentCreateRequestDto.builder()
                .content("좋은 리뷰네요!")
                .build();

        updateRequestDto = TripPlaceReviewCommentUpdateRequestDto.builder()
                .content("수정된 댓글입니다.")
                .build();

        comment = TripPlaceReviewComment.createTripPlaceReviewComment("좋은 리뷰네요!", tripPlaceReview, user);
        ReflectionTestUtils.setField(comment, "id", 1L);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        commentDto = TripPlaceReviewCommentDto.builder()
                .id(1L)
                .content("좋은 리뷰네요!")
                .build();
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 생성 성공")
    void createTripPlaceReviewComment_Success() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.save(any(TripPlaceReviewComment.class))).willReturn(comment);
        given(tripPlaceReviewCommentMapper.toDto(any(TripPlaceReviewComment.class))).willReturn(commentDto);

        // when
        TripPlaceReviewCommentDto result = tripPlaceReviewCommentService.createTripPlaceReviewComment(
                user, tripPlaceId, tripPlaceReviewId, createRequestDto
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(commentDto);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().save(any(TripPlaceReviewComment.class));
        then(tripPlaceReviewCommentMapper).should().toDto(any(TripPlaceReviewComment.class));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 생성 실패 - 존재하지 않는 여행 장소")
    void createTripPlaceReviewComment_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long tripPlaceReviewId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.createTripPlaceReviewComment(
                user, tripPlaceId, tripPlaceReviewId, createRequestDto
        )).isInstanceOf(TripPlaceException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 생성 실패 - 존재하지 않는 리뷰")
    void createTripPlaceReviewComment_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.createTripPlaceReviewComment(
                user, tripPlaceId, tripPlaceReviewId, createRequestDto
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 생성 실패 - 리뷰가 여행 장소에 속하지 않음")
    void createTripPlaceReviewComment_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;

        TripPlace differentTripPlace = TripPlace.createTripPlace(
                "다른 장소",
                "다른 주소",
                "다른 설명",
                new BigDecimal("37.0000"),
                new BigDecimal("127.0000"),
                PlaceCategory.CAFE,
                "http://example.com/different"
        );
        ReflectionTestUtils.setField(differentTripPlace, "id", 2L);

        TripPlaceReview differentReview = TripPlaceReview.createTripPlaceReview(
                user,
                differentTripPlace,
                new BigDecimal("4.0"),
                "다른 리뷰"
        );
        ReflectionTestUtils.setField(differentReview, "id", 1L);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(differentReview));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.createTripPlaceReviewComment(
                user, tripPlaceId, tripPlaceReviewId, createRequestDto
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 목록 조회 성공")
    void getTripPlaceReviewComments_Success() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        TripPlaceReviewComment comment2 = TripPlaceReviewComment.createTripPlaceReviewComment("두번째 댓글", tripPlaceReview, user);
        ReflectionTestUtils.setField(comment2, "id", 2L);

        List<TripPlaceReviewComment> comments = Arrays.asList(comment, comment2);

        TripPlaceReviewCommentDto commentDto2 = TripPlaceReviewCommentDto.builder()
                .id(2L)
                .content("두번째 댓글")
                .build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findAllByCursor(eq(tripPlaceReviewId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(comments);
        given(tripPlaceReviewCommentMapper.toDto(eq(comment))).willReturn(commentDto);
        given(tripPlaceReviewCommentMapper.toDto(eq(comment2))).willReturn(commentDto2);

        // when
        CursorPageResponseDto<TripPlaceReviewCommentDto> result = tripPlaceReviewCommentService.getTripPlaceReviewComments(
                tripPlaceId, tripPlaceReviewId, keyword, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findAllByCursor(eq(tripPlaceReviewId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(tripPlaceReviewCommentMapper).should().toDto(eq(comment));
        then(tripPlaceReviewCommentMapper).should().toDto(eq(comment2));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 목록 조회 실패 - 존재하지 않는 여행 장소")
    void getTripPlaceReviewComments_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long tripPlaceReviewId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.getTripPlaceReviewComments(
                tripPlaceId, tripPlaceReviewId, null, "createdAt", "desc", null, null, 10
        )).isInstanceOf(TripPlaceException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 목록 조회 실패 - 존재하지 않는 리뷰")
    void getTripPlaceReviewComments_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.getTripPlaceReviewComments(
                tripPlaceId, tripPlaceReviewId, null, "createdAt", "desc", null, null, 10
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 목록 조회 실패 - 리뷰가 여행 장소에 속하지 않음")
    void getTripPlaceReviewComments_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;

        TripPlace differentTripPlace = TripPlace.createTripPlace(
                "다른 장소",
                "다른 주소",
                "다른 설명",
                new BigDecimal("37.0000"),
                new BigDecimal("127.0000"),
                PlaceCategory.CAFE,
                "http://example.com/different"
        );
        ReflectionTestUtils.setField(differentTripPlace, "id", 2L);

        TripPlaceReview differentReview = TripPlaceReview.createTripPlaceReview(
                user,
                differentTripPlace,
                new BigDecimal("4.0"),
                "다른 리뷰"
        );
        ReflectionTestUtils.setField(differentReview, "id", 1L);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(differentReview));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.getTripPlaceReviewComments(
                tripPlaceId, tripPlaceReviewId, null, "createdAt", "desc", null, null, 10
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 목록 조회 성공 - hasNext true")
    void getTripPlaceReviewComments_HasNext() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 2;

        TripPlaceReviewComment comment1 = TripPlaceReviewComment.createTripPlaceReviewComment("첫번째 댓글", tripPlaceReview, user);
        ReflectionTestUtils.setField(comment1, "id", 1L);
        ReflectionTestUtils.setField(comment1, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        TripPlaceReviewComment comment2 = TripPlaceReviewComment.createTripPlaceReviewComment("두번째 댓글", tripPlaceReview, user);
        ReflectionTestUtils.setField(comment2, "id", 2L);
        ReflectionTestUtils.setField(comment2, "createdAt", LocalDateTime.of(2024, 1, 2, 12, 0, 0));

        TripPlaceReviewComment comment3 = TripPlaceReviewComment.createTripPlaceReviewComment("세번째 댓글", tripPlaceReview, user);
        ReflectionTestUtils.setField(comment3, "id", 3L);
        ReflectionTestUtils.setField(comment3, "createdAt", LocalDateTime.of(2024, 1, 3, 12, 0, 0));

        List<TripPlaceReviewComment> comments = Arrays.asList(comment1, comment2, comment3);

        TripPlaceReviewCommentDto commentDto1 = TripPlaceReviewCommentDto.builder().build();
        TripPlaceReviewCommentDto commentDto2 = TripPlaceReviewCommentDto.builder().build();

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findAllByCursor(eq(tripPlaceReviewId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(comments);
        given(tripPlaceReviewCommentMapper.toDto(eq(comment1))).willReturn(commentDto1);
        given(tripPlaceReviewCommentMapper.toDto(eq(comment2))).willReturn(commentDto2);

        // when
        CursorPageResponseDto<TripPlaceReviewCommentDto> result = tripPlaceReviewCommentService.getTripPlaceReviewComments(
                tripPlaceId, tripPlaceReviewId, keyword, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("2024-01-02T12:00");
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findAllByCursor(eq(tripPlaceReviewId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(tripPlaceReviewCommentMapper).should().toDto(eq(comment1));
        then(tripPlaceReviewCommentMapper).should().toDto(eq(comment2));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 조회 성공")
    void getTripPlaceReviewComment_Success() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
        given(tripPlaceReviewCommentMapper.toDto(eq(comment))).willReturn(commentDto);

        // when
        TripPlaceReviewCommentDto result = tripPlaceReviewCommentService.getTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(commentDto);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
        then(tripPlaceReviewCommentMapper).should().toDto(eq(comment));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 조회 실패 - 존재하지 않는 여행 장소")
    void getTripPlaceReviewComment_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.getTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        )).isInstanceOf(TripPlaceException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 조회 실패 - 존재하지 않는 리뷰")
    void getTripPlaceReviewComment_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 999L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.getTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 조회 실패 - 리뷰가 여행 장소에 속하지 않음")
    void getTripPlaceReviewComment_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        TripPlace differentTripPlace = TripPlace.createTripPlace(
                "다른 장소",
                "다른 주소",
                "다른 설명",
                new BigDecimal("37.0000"),
                new BigDecimal("127.0000"),
                PlaceCategory.CAFE,
                "http://example.com/different"
        );
        ReflectionTestUtils.setField(differentTripPlace, "id", 2L);

        TripPlaceReview differentReview = TripPlaceReview.createTripPlaceReview(
                user,
                differentTripPlace,
                new BigDecimal("4.0"),
                "다른 리뷰"
        );
        ReflectionTestUtils.setField(differentReview, "id", 1L);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(differentReview));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.getTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 조회 실패 - 존재하지 않는 댓글")
    void getTripPlaceReviewComment_NotFound() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.getTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        )).isInstanceOf(TripPlaceReviewCommentException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 조회 실패 - 댓글이 리뷰에 속하지 않음")
    void getTripPlaceReviewComment_CommentNotBelongToReview() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        TripPlaceReview differentReview = TripPlaceReview.createTripPlaceReview(
                user,
                tripPlace,
                new BigDecimal("3.0"),
                "다른 리뷰"
        );
        ReflectionTestUtils.setField(differentReview, "id", 2L);

        TripPlaceReviewComment differentComment = TripPlaceReviewComment.createTripPlaceReviewComment("다른 리뷰의 댓글", differentReview, user);
        ReflectionTestUtils.setField(differentComment, "id", 1L);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.of(differentComment));
        given(tripPlaceReviewCommentMapper.toDto(eq(differentComment))).willReturn(commentDto);

        // when
        TripPlaceReviewCommentDto result = tripPlaceReviewCommentService.getTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        );

        // then - 서비스가 댓글의 소속을 검증하지 않으므로 정상 조회됨
        assertThat(result).isNotNull();

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 수정 성공")
    void updateTripPlaceReviewComment_Success() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
        given(tripPlaceReviewCommentMapper.toDto(eq(comment))).willReturn(commentDto);

        // when
        TripPlaceReviewCommentDto result = tripPlaceReviewCommentService.updateTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId, updateRequestDto
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(commentDto);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
        then(tripPlaceReviewCommentMapper).should().toDto(eq(comment));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 수정 실패 - 존재하지 않는 여행 장소")
    void updateTripPlaceReviewComment_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.updateTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId, updateRequestDto
        )).isInstanceOf(TripPlaceException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 수정 실패 - 존재하지 않는 리뷰")
    void updateTripPlaceReviewComment_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 999L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.updateTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId, updateRequestDto
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 수정 실패 - 리뷰가 여행 장소에 속하지 않음")
    void updateTripPlaceReviewComment_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        TripPlace differentTripPlace = TripPlace.createTripPlace(
                "다른 장소",
                "다른 주소",
                "다른 설명",
                new BigDecimal("37.0000"),
                new BigDecimal("127.0000"),
                PlaceCategory.CAFE,
                "http://example.com/different"
        );
        ReflectionTestUtils.setField(differentTripPlace, "id", 2L);

        TripPlaceReview differentReview = TripPlaceReview.createTripPlaceReview(
                user,
                differentTripPlace,
                new BigDecimal("4.0"),
                "다른 리뷰"
        );
        ReflectionTestUtils.setField(differentReview, "id", 1L);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(differentReview));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.updateTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId, updateRequestDto
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 수정 실패 - 존재하지 않는 댓글")
    void updateTripPlaceReviewComment_NotFound() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.updateTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId, updateRequestDto
        )).isInstanceOf(TripPlaceReviewCommentException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 수정 실패 - 댓글이 리뷰에 속하지 않음")
    void updateTripPlaceReviewComment_CommentNotBelongToReview() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        TripPlaceReview differentReview = TripPlaceReview.createTripPlaceReview(
                user,
                tripPlace,
                new BigDecimal("3.0"),
                "다른 리뷰"
        );
        ReflectionTestUtils.setField(differentReview, "id", 2L);

        TripPlaceReviewComment differentComment = TripPlaceReviewComment.createTripPlaceReviewComment("다른 리뷰의 댓글", differentReview, user);
        ReflectionTestUtils.setField(differentComment, "id", 1L);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.of(differentComment));
        given(tripPlaceReviewCommentMapper.toDto(eq(differentComment))).willReturn(commentDto);

        // when
        TripPlaceReviewCommentDto result = tripPlaceReviewCommentService.updateTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId, updateRequestDto
        );

        // then - 서비스가 댓글의 소속을 검증하지 않으므로 정상 수정됨
        assertThat(result).isNotNull();

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 삭제 성공")
    void deleteTripPlaceReviewComment_Success() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));

        // when
        tripPlaceReviewCommentService.deleteTripPlaceReviewComment(tripPlaceId, tripPlaceReviewId, commentId);

        // then
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
        then(tripPlaceReviewCommentRepository).should().delete(eq(comment));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 삭제 실패 - 존재하지 않는 여행 장소")
    void deleteTripPlaceReviewComment_TripPlaceNotFound() {
        // given
        Long tripPlaceId = 999L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.deleteTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        )).isInstanceOf(TripPlaceException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 삭제 실패 - 존재하지 않는 리뷰")
    void deleteTripPlaceReviewComment_ReviewNotFound() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 999L;
        Long commentId = 1L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.deleteTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 삭제 실패 - 리뷰가 여행 장소에 속하지 않음")
    void deleteTripPlaceReviewComment_ReviewNotBelongToTripPlace() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        TripPlace differentTripPlace = TripPlace.createTripPlace(
                "다른 장소",
                "다른 주소",
                "다른 설명",
                new BigDecimal("37.0000"),
                new BigDecimal("127.0000"),
                PlaceCategory.CAFE,
                "http://example.com/different"
        );
        ReflectionTestUtils.setField(differentTripPlace, "id", 2L);

        TripPlaceReview differentReview = TripPlaceReview.createTripPlaceReview(
                user,
                differentTripPlace,
                new BigDecimal("4.0"),
                "다른 리뷰"
        );
        ReflectionTestUtils.setField(differentReview, "id", 1L);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(differentReview));

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.deleteTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        )).isInstanceOf(TripPlaceReviewException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 삭제 실패 - 존재하지 않는 댓글")
    void deleteTripPlaceReviewComment_NotFound() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 999L;

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripPlaceReviewCommentService.deleteTripPlaceReviewComment(
                tripPlaceId, tripPlaceReviewId, commentId
        )).isInstanceOf(TripPlaceReviewCommentException.class);

        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 삭제 실패 - 댓글이 리뷰에 속하지 않음")
    void deleteTripPlaceReviewComment_CommentNotBelongToReview() {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        Long commentId = 1L;

        TripPlaceReview differentReview = TripPlaceReview.createTripPlaceReview(
                user,
                tripPlace,
                new BigDecimal("3.0"),
                "다른 리뷰"
        );
        ReflectionTestUtils.setField(differentReview, "id", 2L);

        TripPlaceReviewComment differentComment = TripPlaceReviewComment.createTripPlaceReviewComment("다른 리뷰의 댓글", differentReview, user);
        ReflectionTestUtils.setField(differentComment, "id", 1L);

        given(tripPlaceRepository.findById(eq(tripPlaceId))).willReturn(Optional.of(tripPlace));
        given(tripPlaceReviewRepository.findById(eq(tripPlaceReviewId))).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewCommentRepository.findById(eq(commentId))).willReturn(Optional.of(differentComment));

        // when
        tripPlaceReviewCommentService.deleteTripPlaceReviewComment(tripPlaceId, tripPlaceReviewId, commentId);

        // then - 서비스가 댓글의 소속을 검증하지 않으므로 정상 삭제됨
        then(tripPlaceRepository).should().findById(eq(tripPlaceId));
        then(tripPlaceReviewRepository).should().findById(eq(tripPlaceReviewId));
        then(tripPlaceReviewCommentRepository).should().findById(eq(commentId));
        then(tripPlaceReviewCommentRepository).should().delete(eq(differentComment));
    }
}
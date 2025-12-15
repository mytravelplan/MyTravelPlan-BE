package travel.mytravelplan.domain.review.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.review.dto.*;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.review.service.TripPlaceReviewService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripPlaceReviewController.class)
@DisplayName("여행 장소 리뷰 컨트롤러 테스트")
class TripPlaceReviewControllerTest extends ControllerTestSupport {

    @MockitoBean
    private TripPlaceReviewService tripPlaceReviewService;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        testUser = User.createUser(
                "testuser",
                "password123",
                "test@example.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        ReflectionTestUtils.setField(testUser, "id", 1L);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.USER));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("여행 장소 리뷰 생성 - 성공")
    void createTripPlaceReview_Success() throws Exception {
        // given
        Long tripPlaceId = 1L;
        TripPlaceReviewCreateRequestDto requestDto = TripPlaceReviewCreateRequestDto.builder()
                .content("정말 멋진 장소입니다!")
                .rating(new BigDecimal("4.5"))
                .build();

        TripPlaceReviewDto responseDto = TripPlaceReviewDto.builder()
                .id(1L)
                .placeId(tripPlaceId)
                .userId(1L)
                .username("testuser")
                .content("정말 멋진 장소입니다!")
                .rating(new BigDecimal("4.5"))
                .liked(false)
                .numberOfLikes(0L)
                .numberOfComments(0L)
                .build();

        given(tripPlaceReviewService.createTripPlaceReview(any(User.class), eq(tripPlaceId), any(TripPlaceReviewCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/trip-places/{tripPlaceId}/trip-place-reviews", tripPlaceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("정말 멋진 장소입니다!"))
                .andExpect(jsonPath("$.data.rating").value(4.5))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용"),
                                fieldWithPath("rating").type(JsonFieldType.NUMBER).description("평점 (0.0 ~ 5.0)")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.placeId").type(JsonFieldType.NUMBER).description("여행 장소 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("리뷰 내용"),
                                fieldWithPath("data.rating").type(JsonFieldType.NUMBER).description("평점"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                                fieldWithPath("data.numberOfLikes").type(JsonFieldType.NUMBER).description("좋아요 수"),
                                fieldWithPath("data.numberOfComments").type(JsonFieldType.NUMBER).description("댓글 수")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 조회 - 성공")
    void getTripPlaceReview_Success() throws Exception {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        TripPlaceReviewDto responseDto = TripPlaceReviewDto.builder()
                .id(tripPlaceReviewId)
                .placeId(tripPlaceId)
                .userId(1L)
                .username("testuser")
                .content("정말 멋진 장소입니다!")
                .rating(new BigDecimal("4.5"))
                .liked(true)
                .numberOfLikes(10L)
                .numberOfComments(5L)
                .build();

        given(tripPlaceReviewService.getTripPlaceReview(any(User.class), eq(tripPlaceId), eq(tripPlaceReviewId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}", tripPlaceId, tripPlaceReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tripPlaceReviewId))
                .andExpect(jsonPath("$.data.numberOfLikes").value(10))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("리뷰 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.placeId").type(JsonFieldType.NUMBER).description("여행 장소 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("리뷰 내용"),
                                fieldWithPath("data.rating").type(JsonFieldType.NUMBER).description("평점"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                                fieldWithPath("data.numberOfLikes").type(JsonFieldType.NUMBER).description("좋아요 수"),
                                fieldWithPath("data.numberOfComments").type(JsonFieldType.NUMBER).description("댓글 수")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 목록 조회 - 성공")
    void getTripPlaceReviews_Success() throws Exception {
        // given
        Long tripPlaceId = 1L;
        List<TripPlaceReviewDto> reviews = Arrays.asList(
                TripPlaceReviewDto.builder()
                        .id(1L)
                        .placeId(tripPlaceId)
                        .userId(1L)
                        .username("user1")
                        .content("첫 번째 리뷰")
                        .rating(new BigDecimal("4.5"))
                        .liked(false)
                        .numberOfLikes(10L)
                        .numberOfComments(5L)
                        .build(),
                TripPlaceReviewDto.builder()
                        .id(2L)
                        .placeId(tripPlaceId)
                        .userId(2L)
                        .username("user2")
                        .content("두 번째 리뷰")
                        .rating(new BigDecimal("5.0"))
                        .liked(true)
                        .numberOfLikes(20L)
                        .numberOfComments(3L)
                        .build()
        );

        CursorPageResponseDto<TripPlaceReviewDto> responseDto = CursorPageResponseDto.<TripPlaceReviewDto>builder()
                .content(reviews)
                .hasNext(true)
                .nextCursor("cursor123")
                .nextAfter(2L)
                .build();

        given(tripPlaceReviewService.getTripPlaceReviews(any(User.class), eq(tripPlaceId), eq("멋진"), eq(false), eq(new BigDecimal("4.0")), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/trip-places/{tripPlaceId}/trip-place-reviews", tripPlaceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("keyword", "멋진")
                        .param("imgOnly", "false")
                        .param("rating", "4.0")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("cursor123"))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("imgOnly").description("이미지가 있는 리뷰만 조회 (기본값: false)").optional(),
                                parameterWithName("rating").description("평점 필터").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("리뷰 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.content[].placeId").type(JsonFieldType.NUMBER).description("여행 장소 ID"),
                                fieldWithPath("data.content[].userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.content[].username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content[].content").type(JsonFieldType.STRING).description("리뷰 내용"),
                                fieldWithPath("data.content[].rating").type(JsonFieldType.NUMBER).description("평점"),
                                fieldWithPath("data.content[].liked").type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                                fieldWithPath("data.content[].numberOfLikes").type(JsonFieldType.NUMBER).description("좋아요 수"),
                                fieldWithPath("data.content[].numberOfComments").type(JsonFieldType.NUMBER).description("댓글 수"),
                                fieldWithPath("data.nextCursor").type(JsonFieldType.STRING).description("다음 커서").optional(),
                                fieldWithPath("data.nextAfter").type(JsonFieldType.NUMBER).description("다음 after 값").optional(),
                                fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 수정 - 성공")
    void updateTripPlaceReview_Success() throws Exception {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        TripPlaceReviewUpdateRequestDto requestDto = TripPlaceReviewUpdateRequestDto.builder()
                .content("수정된 리뷰 내용입니다.")
                .rating(new BigDecimal("5.0"))
                .build();

        TripPlaceReviewDto responseDto = TripPlaceReviewDto.builder()
                .id(tripPlaceReviewId)
                .placeId(tripPlaceId)
                .userId(1L)
                .username("testuser")
                .content("수정된 리뷰 내용입니다.")
                .rating(new BigDecimal("5.0"))
                .liked(false)
                .numberOfLikes(10L)
                .numberOfComments(5L)
                .build();

        TripPlaceReview tripPlaceReview = TripPlaceReview.createTripPlaceReview(testUser, null, new BigDecimal("4.5"), "원래 내용");
        ReflectionTestUtils.setField(tripPlaceReview, "id", tripPlaceReviewId);

        given(tripPlaceReviewRepository.findById(tripPlaceReviewId)).willReturn(Optional.of(tripPlaceReview));
        given(tripPlaceReviewService.updateTripPlaceReview(any(User.class), eq(tripPlaceId), eq(tripPlaceReviewId), any(TripPlaceReviewUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}", tripPlaceId, tripPlaceReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("수정된 리뷰 내용입니다."))
                .andExpect(jsonPath("$.data.rating").value(5.0))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("리뷰 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("수정할 리뷰 내용"),
                                fieldWithPath("rating").type(JsonFieldType.NUMBER).description("수정할 평점 (0.0 ~ 5.0)")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.placeId").type(JsonFieldType.NUMBER).description("여행 장소 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("리뷰 내용"),
                                fieldWithPath("data.rating").type(JsonFieldType.NUMBER).description("평점"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                                fieldWithPath("data.numberOfLikes").type(JsonFieldType.NUMBER).description("좋아요 수"),
                                fieldWithPath("data.numberOfComments").type(JsonFieldType.NUMBER).description("댓글 수")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 삭제 - 성공")
    void deleteTripPlaceReview_Success() throws Exception {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;

        TripPlaceReview tripPlaceReview = TripPlaceReview.createTripPlaceReview(testUser, null, new BigDecimal("4.5"), "리뷰 내용");
        ReflectionTestUtils.setField(tripPlaceReview, "id", tripPlaceReviewId);

        given(tripPlaceReviewRepository.findById(tripPlaceReviewId)).willReturn(Optional.of(tripPlaceReview));
        willDoNothing().given(tripPlaceReviewService).deleteTripPlaceReview(eq(tripPlaceId), eq(tripPlaceReviewId));

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}", tripPlaceId, tripPlaceReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("리뷰 ID")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 좋아요 - 성공")
    void likeTripPlaceReview_Success() throws Exception {
        // given
        Long tripPlaceId = 1L;
        Long tripPlaceReviewId = 1L;
        TripPlaceReviewLikeDto responseDto = TripPlaceReviewLikeDto.builder()
                .tripPlaceReviewId(tripPlaceReviewId)
                .userId(1L)
                .liked(true)
                .build();

        given(tripPlaceReviewService.likeTripPlaceReview(any(User.class), eq(tripPlaceId), eq(tripPlaceReviewId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}/like", tripPlaceId, tripPlaceReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripPlaceReviewId").value(tripPlaceReviewId))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-like",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("리뷰 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.tripPlaceReviewId").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 상태 (true: 좋아요, false: 좋아요 취소)")
                        )
                ));
    }
}
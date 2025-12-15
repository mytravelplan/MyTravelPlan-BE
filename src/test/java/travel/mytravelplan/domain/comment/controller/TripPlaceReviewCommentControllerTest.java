package travel.mytravelplan.domain.comment.controller;

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
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentDto;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;
import travel.mytravelplan.domain.comment.service.TripPlaceReviewCommentService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

@WebMvcTest(TripPlaceReviewCommentController.class)
@DisplayName("여행 장소 리뷰 댓글 컨트롤러 테스트")
class TripPlaceReviewCommentControllerTest extends ControllerTestSupport {

    @MockitoBean
    private TripPlaceReviewCommentService tripPlaceReviewCommentService;

    private User testUser;
    private String accessToken;
    private Long tripPlaceId;
    private Long tripPlaceReviewId;

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

        tripPlaceId = 1L;
        tripPlaceReviewId = 1L;
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 생성 - 성공")
    void createTripPlaceReviewComment_Success() throws Exception {
        // given
        TripPlaceReviewCommentCreateRequestDto requestDto = TripPlaceReviewCommentCreateRequestDto.builder()
                .content("여행 장소 리뷰 댓글 내용입니다.")
                .build();

        TripPlaceReviewCommentDto responseDto = TripPlaceReviewCommentDto.builder()
                .id(1L)
                .reviewId(tripPlaceReviewId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("여행 장소 리뷰 댓글 내용입니다.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(tripPlaceReviewCommentService.createTripPlaceReviewComment(any(User.class), eq(tripPlaceId), eq(tripPlaceReviewId), any(TripPlaceReviewCommentCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}/trip-place-review-comments", tripPlaceId, tripPlaceReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("여행 장소 리뷰 댓글 내용입니다."))
                .andExpect(jsonPath("$.data.reviewId").value(tripPlaceReviewId))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-comment-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("여행 장소 리뷰 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("댓글 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.reviewId").type(JsonFieldType.NUMBER).description("여행 장소 리뷰 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 조회 - 성공")
    void getTripPlaceReviewComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        TripPlaceReviewCommentDto responseDto = TripPlaceReviewCommentDto.builder()
                .id(commentId)
                .reviewId(tripPlaceReviewId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("여행 장소 리뷰 댓글 내용입니다.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(tripPlaceReviewCommentService.getTripPlaceReviewComment(eq(tripPlaceId), eq(tripPlaceReviewId), eq(commentId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}/trip-place-review-comments/{tripPlaceReviewCommentId}", tripPlaceId, tripPlaceReviewId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId))
                .andExpect(jsonPath("$.data.content").value("여행 장소 리뷰 댓글 내용입니다."))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-comment-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("여행 장소 리뷰 ID"),
                                parameterWithName("tripPlaceReviewCommentId").description("댓글 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.reviewId").type(JsonFieldType.NUMBER).description("여행 장소 리뷰 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 목록 조회 - 성공")
    void getTripPlaceReviewComments_Success() throws Exception {
        // given
        List<TripPlaceReviewCommentDto> comments = Arrays.asList(
                TripPlaceReviewCommentDto.builder()
                        .id(1L)
                        .reviewId(tripPlaceReviewId)
                        .userId(1L)
                        .username("user1")
                        .content("첫 번째 댓글")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                TripPlaceReviewCommentDto.builder()
                        .id(2L)
                        .reviewId(tripPlaceReviewId)
                        .userId(2L)
                        .username("user2")
                        .content("두 번째 댓글")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        CursorPageResponseDto<TripPlaceReviewCommentDto> responseDto = CursorPageResponseDto.<TripPlaceReviewCommentDto>builder()
                .content(comments)
                .hasNext(false)
                .nextCursor(null)
                .nextAfter(null)
                .build();

        given(tripPlaceReviewCommentService.getTripPlaceReviewComments(eq(tripPlaceId), eq(tripPlaceReviewId), eq(null), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}/trip-place-review-comments", tripPlaceId, tripPlaceReviewId)
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-comment-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("여행 장소 리뷰 ID")
                        ),
                        queryParameters(
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("댓글 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.content[].reviewId").type(JsonFieldType.NUMBER).description("여행 장소 리뷰 ID"),
                                fieldWithPath("data.content[].userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.content[].username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content[].content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.content[].updatedAt").type(JsonFieldType.STRING).description("수정일시"),
                                fieldWithPath("data.nextCursor").type(JsonFieldType.NULL).description("다음 커서").optional(),
                                fieldWithPath("data.nextAfter").type(JsonFieldType.NULL).description("다음 after 값").optional(),
                                fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 수정 - 성공")
    void updateTripPlaceReviewComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        TripPlaceReviewCommentUpdateRequestDto requestDto = TripPlaceReviewCommentUpdateRequestDto.builder()
                .content("수정된 댓글 내용")
                .build();

        TripPlaceReviewCommentDto responseDto = TripPlaceReviewCommentDto.builder()
                .id(commentId)
                .reviewId(tripPlaceReviewId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("수정된 댓글 내용")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        TripPlaceReviewComment mockComment = TripPlaceReviewComment.createTripPlaceReviewComment("기존 내용", null, testUser);
        ReflectionTestUtils.setField(mockComment, "id", commentId);

        given(tripPlaceReviewCommentRepository.findById(eq(commentId)))
                .willReturn(Optional.of(mockComment));

        given(tripPlaceReviewCommentService.updateTripPlaceReviewComment(eq(tripPlaceId), eq(tripPlaceReviewId), eq(commentId), any(TripPlaceReviewCommentUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}/trip-place-review-comments/{tripPlaceReviewCommentId}", tripPlaceId, tripPlaceReviewId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId))
                .andExpect(jsonPath("$.data.content").value("수정된 댓글 내용"))
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-comment-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("여행 장소 리뷰 ID"),
                                parameterWithName("tripPlaceReviewCommentId").description("댓글 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("수정할 댓글 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.reviewId").type(JsonFieldType.NUMBER).description("여행 장소 리뷰 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("여행 장소 리뷰 댓글 삭제 - 성공")
    void deleteTripPlaceReviewComment_Success() throws Exception {
        // given
        Long commentId = 1L;

        TripPlaceReviewComment mockComment = TripPlaceReviewComment.createTripPlaceReviewComment("삭제할 댓글", null, testUser);
        ReflectionTestUtils.setField(mockComment, "id", commentId);

        given(tripPlaceReviewCommentRepository.findById(eq(commentId)))
                .willReturn(Optional.of(mockComment));

        willDoNothing().given(tripPlaceReviewCommentService).deleteTripPlaceReviewComment(eq(tripPlaceId), eq(tripPlaceReviewId), eq(commentId));

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}/trip-place-review-comments/{tripPlaceReviewCommentId}", tripPlaceId, tripPlaceReviewId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("trip-place-review-comment-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("tripPlaceId").description("여행 장소 ID"),
                                parameterWithName("tripPlaceReviewId").description("여행 장소 리뷰 ID"),
                                parameterWithName("tripPlaceReviewCommentId").description("댓글 ID")
                        )
                ));
    }
}
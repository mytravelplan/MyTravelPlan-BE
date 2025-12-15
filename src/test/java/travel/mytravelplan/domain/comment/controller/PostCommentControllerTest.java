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
import travel.mytravelplan.domain.comment.dto.PostCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.PostCommentDto;
import travel.mytravelplan.domain.comment.dto.PostCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.entity.PostComment;
import travel.mytravelplan.domain.comment.service.PostCommentService;
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

@WebMvcTest(PostCommentController.class)
@DisplayName("게시물 댓글 컨트롤러 테스트")
public class PostCommentControllerTest extends ControllerTestSupport {

    @MockitoBean
    private PostCommentService postCommentService;

    private User testUser;
    private String accessToken;
    private Long postId;

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

        postId = 1L;
    }

    @Test
    @DisplayName("게시물 댓글 작성 - 성공")
    void createPostComment_Success() throws Exception {
        // given
        PostCommentCreateRequestDto requestDto = PostCommentCreateRequestDto.builder()
                .content("댓글 내용입니다.")
                .build();

        PostCommentDto responseDto = PostCommentDto.builder()
                .id(1L)
                .postId(postId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("댓글 내용입니다.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postCommentService.createPostComment(any(User.class), eq(postId), any(PostCommentCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/post-comments", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("댓글 내용입니다."))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andDo(MockMvcRestDocumentationWrapper.document("post-comment-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("댓글 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("게시물 댓글 조회 - 성공")
    void getPostComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        PostCommentDto responseDto = PostCommentDto.builder()
                .id(commentId)
                .postId(postId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("댓글 내용입니다.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postCommentService.getPostComment(eq(postId), eq(commentId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/post-comments/{postCommentId}", postId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId))
                .andExpect(jsonPath("$.data.content").value("댓글 내용입니다."))
                .andDo(MockMvcRestDocumentationWrapper.document("post-comment-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID"),
                                parameterWithName("postCommentId").description("댓글 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("게시물 댓글 목록 조회 - 성공")
    void getPostComments_Success() throws Exception {
        // given
        List<PostCommentDto> comments = Arrays.asList(
                PostCommentDto.builder()
                        .id(1L)
                        .postId(postId)
                        .userId(1L)
                        .username("user1")
                        .content("첫 번째 댓글")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                PostCommentDto.builder()
                        .id(2L)
                        .postId(postId)
                        .userId(2L)
                        .username("user2")
                        .content("두 번째 댓글")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        CursorPageResponseDto<PostCommentDto> responseDto = CursorPageResponseDto.<PostCommentDto>builder()
                .content(comments)
                .hasNext(false)
                .nextCursor(null)
                .nextAfter(null)
                .build();

        given(postCommentService.getPostComments(eq(postId), eq(null), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}/post-comments", postId)
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(MockMvcRestDocumentationWrapper.document("post-comment-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
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
                                fieldWithPath("data.content[].postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
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
    @DisplayName("게시물 댓글 수정 - 성공")
    void updatePostComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        PostCommentUpdateRequestDto requestDto = PostCommentUpdateRequestDto.builder()
                .content("수정된 댓글 내용")
                .build();

        PostCommentDto responseDto = PostCommentDto.builder()
                .id(commentId)
                .postId(postId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("수정된 댓글 내용")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        PostComment mockComment = PostComment.createPostComment("기존 내용", null, testUser);
        ReflectionTestUtils.setField(mockComment, "id", commentId);

        given(postCommentRepository.findById(eq(commentId)))
                .willReturn(Optional.of(mockComment));

        given(postCommentService.updatePostComment(eq(postId), eq(commentId), any(PostCommentUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/posts/{postId}/post-comments/{postCommentId}", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId))
                .andExpect(jsonPath("$.data.content").value("수정된 댓글 내용"))
                .andDo(MockMvcRestDocumentationWrapper.document("post-comment-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID"),
                                parameterWithName("postCommentId").description("댓글 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("수정할 댓글 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("게시물 댓글 삭제 - 성공")
    void deletePostComment_Success() throws Exception {
        // given
        Long commentId = 1L;

        PostComment mockComment = PostComment.createPostComment("삭제할 댓글", null, testUser);
        ReflectionTestUtils.setField(mockComment, "id", commentId);

        given(postCommentRepository.findById(eq(commentId)))
                .willReturn(Optional.of(mockComment));

        willDoNothing().given(postCommentService).deletePostComment(eq(postId), eq(commentId));

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}/post-comments/{postCommentId}", postId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("post-comment-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID"),
                                parameterWithName("postCommentId").description("댓글 ID")
                        )
                ));
    }
}

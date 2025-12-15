package travel.mytravelplan.domain.post.controller;

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
import travel.mytravelplan.domain.post.dto.*;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.service.PostService;
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

@WebMvcTest(PostController.class)
@DisplayName("게시물 컨트롤러 테스트")
class PostControllerTest extends ControllerTestSupport {

    @MockitoBean
    private PostService postService;

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
    @DisplayName("게시글 작성 - 성공")
    void createPost_Success() throws Exception {
        // given
        PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
                .content("테스트 게시글 내용입니다.")
                .imageUrls(Arrays.asList("https://example.com/image1.jpg", "https://example.com/image2.jpg"))
                .hashTags(Arrays.asList("여행", "맛집"))
                .build();

        PostDto responseDto = PostDto.builder()
                .id(1L)
                .authorProfileImageUrl("https://example.com/profile.jpg")
                .content("테스트 게시글 내용입니다.")
                .imageUrls(Arrays.asList("https://example.com/image1.jpg", "https://example.com/image2.jpg"))
                .hashTags(Arrays.asList("여행", "맛집"))
                .numberOfLikes(0)
                .numberOfComments(0)
                .liked(false)
                .bookmarked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.createPost(any(User.class), any(PostCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("테스트 게시글 내용입니다."))
                .andExpect(jsonPath("$.data.imageUrls[0]").value("https://example.com/image1.jpg"))
                .andExpect(jsonPath("$.data.hashTags[0]").value("여행"))
                .andDo(MockMvcRestDocumentationWrapper.document("post-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용"),
                                fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록").optional(),
                                fieldWithPath("hashTags").type(JsonFieldType.ARRAY).description("해시태그 목록").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.authorProfileImageUrl").type(JsonFieldType.STRING).description("작성자 프로필 이미지 URL").optional(),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("게시글 내용"),
                                fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록").optional(),
                                fieldWithPath("data.hashTags").type(JsonFieldType.ARRAY).description("해시태그 목록").optional(),
                                fieldWithPath("data.numberOfLikes").type(JsonFieldType.NUMBER).description("좋아요 수"),
                                fieldWithPath("data.numberOfComments").type(JsonFieldType.NUMBER).description("댓글 수"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                                fieldWithPath("data.bookmarked").type(JsonFieldType.BOOLEAN).description("북마크 여부"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 조회 - 성공")
    void getPost_Success() throws Exception {
        // given
        Long postId = 1L;
        PostDto responseDto = PostDto.builder()
                .id(postId)
                .authorProfileImageUrl("https://example.com/profile.jpg")
                .content("테스트 게시글 내용입니다.")
                .imageUrls(List.of("https://example.com/image1.jpg"))
                .hashTags(List.of("여행"))
                .numberOfLikes(10)
                .numberOfComments(5)
                .liked(true)
                .bookmarked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.getPost(any(User.class), eq(postId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts/{postId}", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.numberOfLikes").value(10))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andDo(MockMvcRestDocumentationWrapper.document("post-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.authorProfileImageUrl").type(JsonFieldType.STRING).description("작성자 프로필 이미지 URL").optional(),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("게시글 내용"),
                                fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록").optional(),
                                fieldWithPath("data.hashTags").type(JsonFieldType.ARRAY).description("해시태그 목록").optional(),
                                fieldWithPath("data.numberOfLikes").type(JsonFieldType.NUMBER).description("좋아요 수"),
                                fieldWithPath("data.numberOfComments").type(JsonFieldType.NUMBER).description("댓글 수"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                                fieldWithPath("data.bookmarked").type(JsonFieldType.BOOLEAN).description("북마크 여부"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 목록 조회 - 성공")
    void getPostsByCursor_Success() throws Exception {
        // given
        List<PostDto> posts = Arrays.asList(
                PostDto.builder()
                        .id(1L)
                        .authorProfileImageUrl("https://example.com/profile1.jpg")
                        .content("첫 번째 게시글")
                        .imageUrls(List.of("https://example.com/image1.jpg"))
                        .hashTags(List.of("여행"))
                        .numberOfLikes(10)
                        .numberOfComments(5)
                        .liked(false)
                        .bookmarked(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                PostDto.builder()
                        .id(2L)
                        .authorProfileImageUrl("https://example.com/profile2.jpg")
                        .content("두 번째 게시글")
                        .imageUrls(List.of("https://example.com/image2.jpg"))
                        .hashTags(List.of("맛집"))
                        .numberOfLikes(20)
                        .numberOfComments(3)
                        .liked(true)
                        .bookmarked(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        CursorPageResponseDto<PostDto> responseDto = CursorPageResponseDto.<PostDto>builder()
                .content(posts)
                .hasNext(true)
                .nextCursor("cursor123")
                .nextAfter(2L)
                .build();

        given(postService.getPosts(any(User.class), eq("여행"), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("keyword", "여행")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("cursor123"))
                .andDo(MockMvcRestDocumentationWrapper.document("post-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("게시글 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.content[].authorProfileImageUrl").type(JsonFieldType.STRING).description("작성자 프로필 이미지 URL").optional(),
                                fieldWithPath("data.content[].content").type(JsonFieldType.STRING).description("게시글 내용"),
                                fieldWithPath("data.content[].imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록").optional(),
                                fieldWithPath("data.content[].hashTags").type(JsonFieldType.ARRAY).description("해시태그 목록").optional(),
                                fieldWithPath("data.content[].numberOfLikes").type(JsonFieldType.NUMBER).description("좋아요 수"),
                                fieldWithPath("data.content[].numberOfComments").type(JsonFieldType.NUMBER).description("댓글 수"),
                                fieldWithPath("data.content[].liked").type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                                fieldWithPath("data.content[].bookmarked").type(JsonFieldType.BOOLEAN).description("북마크 여부"),
                                fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.content[].updatedAt").type(JsonFieldType.STRING).description("수정일시"),
                                fieldWithPath("data.nextCursor").type(JsonFieldType.STRING).description("다음 커서").optional(),
                                fieldWithPath("data.nextAfter").type(JsonFieldType.NUMBER).description("다음 after 값").optional(),
                                fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void updatePost_Success() throws Exception {
        // given
        Long postId = 1L;
        PostUpdateRequestDto requestDto = PostUpdateRequestDto.builder()
                .content("수정된 게시글 내용입니다.")
                .imageUrls(List.of("https://example.com/new-image.jpg"))
                .hashTags(Arrays.asList("수정", "테스트"))
                .build();

        PostDto responseDto = PostDto.builder()
                .id(postId)
                .authorProfileImageUrl("https://example.com/profile.jpg")
                .content("수정된 게시글 내용입니다.")
                .imageUrls(List.of("https://example.com/new-image.jpg"))
                .hashTags(Arrays.asList("수정", "테스트"))
                .numberOfLikes(10)
                .numberOfComments(5)
                .liked(false)
                .bookmarked(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        Post mockPost = Post.createPost("기존 내용", List.of(), testUser, List.of());
        ReflectionTestUtils.setField(mockPost, "id", postId);

        given(postRepository.findById(eq(postId)))
                .willReturn(Optional.of(mockPost));

        given(postService.updatePost(any(User.class), eq(postId), any(PostUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/posts/{postId}", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.content").value("수정된 게시글 내용입니다."))
                .andDo(MockMvcRestDocumentationWrapper.document("post-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("수정할 게시글 내용").optional(),
                                fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).description("수정할 이미지 URL 목록").optional(),
                                fieldWithPath("hashTags").type(JsonFieldType.ARRAY).description("수정할 해시태그 목록").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.authorProfileImageUrl").type(JsonFieldType.STRING).description("작성자 프로필 이미지 URL").optional(),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("게시글 내용"),
                                fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록").optional(),
                                fieldWithPath("data.hashTags").type(JsonFieldType.ARRAY).description("해시태그 목록").optional(),
                                fieldWithPath("data.numberOfLikes").type(JsonFieldType.NUMBER).description("좋아요 수"),
                                fieldWithPath("data.numberOfComments").type(JsonFieldType.NUMBER).description("댓글 수"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 여부"),
                                fieldWithPath("data.bookmarked").type(JsonFieldType.BOOLEAN).description("북마크 여부"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void deletePost_Success() throws Exception {
        // given
        Long postId = 1L;

        Post mockPost = Post.createPost("삭제할 게시글", List.of(), testUser, List.of());
        ReflectionTestUtils.setField(mockPost, "id", postId);

        given(postRepository.findById(eq(postId)))
                .willReturn(Optional.of(mockPost));

        willDoNothing().given(postService).deletePost(eq(postId));

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{postId}", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("post-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 좋아요 - 성공")
    void likePost_Success() throws Exception {
        // given
        Long postId = 1L;
        PostLikeDto responseDto = PostLikeDto.builder()
                .postId(postId)
                .userId(testUser.getId())
                .liked(true)
                .build();

        given(postService.likePost(any(User.class), eq(postId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/like", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andDo(MockMvcRestDocumentationWrapper.document("post-like",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 상태 (true: 좋아요, false: 좋아요 취소)")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 북마크 - 성공")
    void bookmarkPost_Success() throws Exception {
        // given
        Long postId = 1L;
        PostBookMarkDto responseDto = PostBookMarkDto.builder()
                .postId(postId)
                .userId(testUser.getId())
                .bookmarked(true)
                .build();

        given(postService.bookmarkPost(any(User.class), eq(postId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/posts/{postId}/bookmark", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.bookmarked").value(true))
                .andDo(MockMvcRestDocumentationWrapper.document("post-bookmark",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
                                fieldWithPath("data.bookmarked").type(JsonFieldType.BOOLEAN).description("북마크 상태 (true: 북마크, false: 북마크 취소)")
                        )
                ));
    }
}
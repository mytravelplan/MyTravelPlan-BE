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
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentDto;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.entity.ProductReviewComment;
import travel.mytravelplan.domain.comment.service.ProductReviewCommentService;
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

@WebMvcTest(ProductReviewCommentController.class)
@DisplayName("상품 리뷰 댓글 컨트롤러 테스트")
class ProductReviewCommentControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ProductReviewCommentService productReviewCommentService;

    private User testUser;
    private String accessToken;
    private Long productId;
    private Long productReviewId;

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
                Set.of(Role.SELLER)
        );

        ReflectionTestUtils.setField(testUser, "id", 1L);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.SELLER));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        productId = 1L;
        productReviewId = 1L;
    }

    @Test
    @DisplayName("상품 리뷰 댓글 생성 - 성공")
    void createProductReviewComment_Success() throws Exception {
        // given
        ProductReviewCommentCreateRequestDto requestDto = ProductReviewCommentCreateRequestDto.builder()
                .content("상품 리뷰에 대한 답변입니다.")
                .build();

        ProductReviewCommentDto responseDto = ProductReviewCommentDto.builder()
                .id(1L)
                .productReviewId(productReviewId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("상품 리뷰에 대한 답변입니다.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(productReviewCommentService.createProductReviewComment(any(User.class), eq(productId), eq(productReviewId), any(ProductReviewCommentCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/products/{productId}/product-reviews/{productReviewId}/product-review-comments", productId, productReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("상품 리뷰에 대한 답변입니다."))
                .andExpect(jsonPath("$.data.productReviewId").value(productReviewId))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-comment-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("상품 리뷰 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("댓글 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.productReviewId").type(JsonFieldType.NUMBER).description(" 상품 리뷰 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 조회 - 성공")
    void getProductReviewComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        ProductReviewCommentDto responseDto = ProductReviewCommentDto.builder()
                .id(commentId)
                .productReviewId(productReviewId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("상품 리뷰 댓글 내용입니다.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(productReviewCommentService.getProductReviewComment(eq(productId), eq(productReviewId), eq(commentId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/products/{productId}/product-reviews/{productReviewId}/product-review-comments/{productReviewCommentId}", productId, productReviewId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId))
                .andExpect(jsonPath("$.data.content").value("상품 리뷰 댓글 내용입니다."))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-comment-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("상품 리뷰 ID"),
                                parameterWithName("productReviewCommentId").description("댓글 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.productReviewId").type(JsonFieldType.NUMBER).description(" 상품 리뷰 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 목록 조회 - 성공")
    void getProductReviewComments_Success() throws Exception {
        // given
        List<ProductReviewCommentDto> comments = Arrays.asList(
                ProductReviewCommentDto.builder()
                        .id(1L)
                        .productReviewId(productReviewId)
                        .userId(1L)
                        .username("seller1")
                        .content("첫 번째 댓글")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                ProductReviewCommentDto.builder()
                        .id(2L)
                        .productReviewId(productReviewId)
                        .userId(2L)
                        .username("seller2")
                        .content("두 번째 댓글")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        CursorPageResponseDto<ProductReviewCommentDto> responseDto = CursorPageResponseDto.<ProductReviewCommentDto>builder()
                .content(comments)
                .hasNext(false)
                .nextCursor(null)
                .nextAfter(null)
                .build();

        given(productReviewCommentService.getProductReviewComments(eq(productId), eq(productReviewId), eq(null), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/products/{productId}/product-reviews/{productReviewId}/product-review-comments", productId, productReviewId)
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-comment-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("상품 리뷰 ID")
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
                                fieldWithPath("data.content[].productReviewId").type(JsonFieldType.NUMBER).description("상품 리뷰 ID"),
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
    @DisplayName("상품 리뷰 댓글 목록 조회 (키워드 포함) - 성공")
    void getProductReviewCommentsWithKeyword_Success() throws Exception {
        // given
        String keyword = "답변";
        List<ProductReviewCommentDto> comments = Arrays.asList(
                ProductReviewCommentDto.builder()
                        .id(1L)
                        .productReviewId(productReviewId)
                        .userId(1L)
                        .username("seller1")
                        .content("답변 내용입니다")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        CursorPageResponseDto<ProductReviewCommentDto> responseDto = CursorPageResponseDto.<ProductReviewCommentDto>builder()
                .content(comments)
                .hasNext(false)
                .nextCursor(null)
                .nextAfter(null)
                .build();

        given(productReviewCommentService.getProductReviewComments(eq(productId), eq(productReviewId), eq(keyword), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/products/{productId}/product-reviews/{productReviewId}/product-review-comments", productId, productReviewId)
                        .param("keyword", keyword)
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 수정 - 성공")
    void updateProductReviewComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        ProductReviewCommentUpdateRequestDto requestDto = ProductReviewCommentUpdateRequestDto.builder()
                .content("수정된 댓글 내용")
                .build();

        ProductReviewCommentDto responseDto = ProductReviewCommentDto.builder()
                .id(commentId)
                .productReviewId(productReviewId)
                .userId(testUser.getId())
                .username(testUser.getUsername())
                .content("수정된 댓글 내용")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        ProductReviewComment mockComment = ProductReviewComment.createProductReviewComment("기존 내용", null, testUser);
        ReflectionTestUtils.setField(mockComment, "id", commentId);

        given(productReviewCommentRepository.findById(eq(commentId)))
                .willReturn(Optional.of(mockComment));

        given(productReviewCommentService.updateProductReviewComment(eq(productId), eq(productReviewId), eq(commentId), any(ProductReviewCommentUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/products/{productId}/product-reviews/{productReviewId}/product-review-comments/{productReviewCommentId}", productId, productReviewId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId))
                .andExpect(jsonPath("$.data.content").value("수정된 댓글 내용"))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-comment-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("상품 리뷰 ID"),
                                parameterWithName("productReviewCommentId").description("댓글 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("수정할 댓글 내용")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("댓글 ID"),
                                fieldWithPath("data.productReviewId").type(JsonFieldType.NUMBER).description("상품 리뷰 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("작성자 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("작성자 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("댓글 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시")
                        )
                ));
    }

    @Test
    @DisplayName("상품 리뷰 댓글 삭제 - 성공")
    void deleteProductReviewComment_Success() throws Exception {
        // given
        Long commentId = 1L;

        ProductReviewComment mockComment = ProductReviewComment.createProductReviewComment("삭제할 댓글", null, testUser);
        ReflectionTestUtils.setField(mockComment, "id", commentId);

        given(productReviewCommentRepository.findById(eq(commentId)))
                .willReturn(Optional.of(mockComment));

        willDoNothing().given(productReviewCommentService).deleteProductReviewComment(eq(productId), eq(productReviewId), eq(commentId));

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/products/{productId}/product-reviews/{productReviewId}/product-review-comments/{productReviewCommentId}", productId, productReviewId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-comment-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("상품 리뷰 ID"),
                                parameterWithName("productReviewCommentId").description("댓글 ID")
                        )
                ));
    }
}
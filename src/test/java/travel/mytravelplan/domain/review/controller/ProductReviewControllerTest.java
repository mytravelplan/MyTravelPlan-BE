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
import travel.mytravelplan.domain.review.entity.ProductReview;
import travel.mytravelplan.domain.review.repository.ProductReviewRepository;
import travel.mytravelplan.domain.review.service.ProductReviewService;
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

@WebMvcTest(ProductReviewController.class)
@DisplayName("상품 리뷰 컨트롤러 테스트")
class ProductReviewControllerTest extends ControllerTestSupport {
    @MockitoBean
    private ProductReviewService productReviewService;

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
    @DisplayName("상품 리뷰 생성 - 성공")
    void createProductReview_Success() throws Exception {
        // given
        Long productId = 1L;
        ProductReviewCreateRequestDto requestDto = ProductReviewCreateRequestDto.builder()
                .content("정말 좋은 상품입니다!")
                .rating(new BigDecimal("4.5"))
                .build();

        ProductReviewDto responseDto = ProductReviewDto.builder()
                .id(1L)
                .productId(productId)
                .userId(1L)
                .username("testuser")
                .content("정말 좋은 상품입니다!")
                .rating(new BigDecimal("4.5"))
                .liked(false)
                .numberOfLikes(0)
                .numberOfComments(0)
                .build();

        given(productReviewService.createProductReview(any(User.class), eq(productId), any(ProductReviewCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/products/{productId}/product-reviews", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("정말 좋은 상품입니다!"))
                .andExpect(jsonPath("$.data.rating").value(4.5))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용"),
                                fieldWithPath("rating").type(JsonFieldType.NUMBER).description("평점 (0.0 ~ 5.0)")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.productId").type(JsonFieldType.NUMBER).description("상품 ID"),
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
    @DisplayName("상품 리뷰 조회 - 성공")
    void getProductReview_Success() throws Exception {
        // given
        Long productId = 1L;
        Long productReviewId = 1L;
        ProductReviewDto responseDto = ProductReviewDto.builder()
                .id(productReviewId)
                .productId(productId)
                .userId(1L)
                .username("testuser")
                .content("정말 좋은 상품입니다!")
                .rating(new BigDecimal("4.5"))
                .liked(true)
                .numberOfLikes(10)
                .numberOfComments(5)
                .build();

        given(productReviewService.getProductReview(any(User.class), eq(productId), eq(productReviewId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/products/{productId}/product-reviews/{productReviewId}", productId, productReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productReviewId))
                .andExpect(jsonPath("$.data.numberOfLikes").value(10))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("리뷰 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.productId").type(JsonFieldType.NUMBER).description("상품 ID"),
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
    @DisplayName("상품 리뷰 목록 조회 - 성공")
    void getProductReviews_Success() throws Exception {
        // given
        Long productId = 1L;
        List<ProductReviewDto> reviews = Arrays.asList(
                ProductReviewDto.builder()
                        .id(1L)
                        .productId(productId)
                        .userId(1L)
                        .username("user1")
                        .content("첫 번째 리뷰")
                        .rating(new BigDecimal("4.5"))
                        .liked(false)
                        .numberOfLikes(10)
                        .numberOfComments(5)
                        .build(),
                ProductReviewDto.builder()
                        .id(2L)
                        .productId(productId)
                        .userId(2L)
                        .username("user2")
                        .content("두 번째 리뷰")
                        .rating(new BigDecimal("5.0"))
                        .liked(true)
                        .numberOfLikes(20)
                        .numberOfComments(3)
                        .build()
        );

        CursorPageResponseDto<ProductReviewDto> responseDto = CursorPageResponseDto.<ProductReviewDto>builder()
                .content(reviews)
                .hasNext(true)
                .nextCursor("cursor123")
                .nextAfter(2L)
                .build();

        given(productReviewService.getProductReviews(any(User.class), eq(productId), eq("좋은"), eq(false), eq(null), eq("createdAt"), eq("ASC"), eq(null), eq(null), eq(10)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/products/{productId}/product-reviews", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("keyword", "좋은")
                        .param("imgOnly", "false")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("cursor123"))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("imgOnly").description("이미지가 있는 리뷰만 조회 (기본값: false)").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("리뷰 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.content[].productId").type(JsonFieldType.NUMBER).description("상품 ID"),
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
    @DisplayName("상품 리뷰 수정 - 성공")
    void updateProductReview_Success() throws Exception {
        // given
        Long productId = 1L;
        Long productReviewId = 1L;
        ProductReviewUpdateRequestDto requestDto = ProductReviewUpdateRequestDto.builder()
                .content("수정된 리뷰 내용입니다.")
                .rating(new BigDecimal("5.0"))
                .build();

        ProductReviewDto responseDto = ProductReviewDto.builder()
                .id(productReviewId)
                .productId(productId)
                .userId(1L)
                .username("testuser")
                .content("수정된 리뷰 내용입니다.")
                .rating(new BigDecimal("5.0"))
                .liked(false)
                .numberOfLikes(10)
                .numberOfComments(5)
                .build();

        ProductReview productReview = ProductReview.createProductReview(testUser, null, new BigDecimal("4.5"), "원래 내용");
        ReflectionTestUtils.setField(productReview, "id", productReviewId);

        given(productReviewRepository.findById(productReviewId)).willReturn(Optional.of(productReview));
        given(productReviewService.updateProductReview(any(User.class), eq(productId), eq(productReviewId), any(ProductReviewUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/products/{productId}/product-reviews/{productReviewId}", productId, productReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("수정된 리뷰 내용입니다."))
                .andExpect(jsonPath("$.data.rating").value(5.0))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("리뷰 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("수정할 리뷰 내용"),
                                fieldWithPath("rating").type(JsonFieldType.NUMBER).description("수정할 평점 (0.0 ~ 5.0)")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.productId").type(JsonFieldType.NUMBER).description("상품 ID"),
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
    @DisplayName("상품 리뷰 삭제 - 성공")
    void deleteProductReview_Success() throws Exception {
        // given
        Long productId = 1L;
        Long productReviewId = 1L;

        ProductReview productReview = ProductReview.createProductReview(testUser, null, new BigDecimal("4.5"), "리뷰 내용");
        ReflectionTestUtils.setField(productReview, "id", productReviewId);

        given(productReviewRepository.findById(productReviewId)).willReturn(Optional.of(productReview));
        willDoNothing().given(productReviewService).deleteProductReview(eq(productId), eq(productReviewId));

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/products/{productId}/product-reviews/{productReviewId}", productId, productReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("리뷰 ID")
                        )
                ));
    }

    @Test
    @DisplayName("상품 리뷰 좋아요 - 성공")
    void likeProductReview_Success() throws Exception {
        // given
        Long productId = 1L;
        Long productReviewId = 1L;
        ProductReviewLikeDto responseDto = ProductReviewLikeDto.builder()
                .reviewId(productReviewId)
                .userId(1L)
                .liked(true)
                .build();

        given(productReviewService.likeProductReview(any(User.class), eq(productId), eq(productReviewId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/products/{productId}/product-reviews/{productReviewId}/like", productId, productReviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewId").value(productReviewId))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andDo(MockMvcRestDocumentationWrapper.document("product-review-like",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID"),
                                parameterWithName("productReviewId").description("리뷰 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data.reviewId").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                                fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
                                fieldWithPath("data.liked").type(JsonFieldType.BOOLEAN).description("좋아요 상태 (true: 좋아요, false: 좋아요 취소)")
                        )
                ));
    }
}
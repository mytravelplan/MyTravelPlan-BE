package travel.mytravelplan.domain.product.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.product.dto.*;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.service.ProductService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@DisplayName("상품 컨트롤러 테스트")
class ProductControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ProductService productService;

    private String accessToken;
    private String sellerAccessToken;
    private User testUser;
    private User sellerUser;
    private Long productId;
    private ProductCreateRequestDto createRequestDto;
    private ProductUpdateRequestDto updateRequestDto;
    private ProductDto productDto;
    private Product product;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트 사용자",
                "http://example.com/user.jpg"
        );

        testUser = User.createUser(
                "testUser",
                "password",
                "user@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1995, 5, 15),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        UserProfile sellerProfile = UserProfile.createUserProfile(
                "판매자",
                "http://example.com/seller.jpg"
        );

        sellerUser = User.createUser(
                "sellerUser",
                "password",
                "seller@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-5678-1234",
                Gender.FEMALE,
                Set.of(Role.SELLER)
        );

        testUser.setUserProfile(userProfile);
        sellerUser.setUserProfile(sellerProfile);

        ReflectionTestUtils.setField(testUser, "id", 1L);
        ReflectionTestUtils.setField(sellerUser, "id", 2L);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.USER));
        sellerAccessToken = jwtUtils.createAccessToken(2L, Set.of(Role.SELLER));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.findById(2L)).willReturn(Optional.of(sellerUser));

        productId = 1L;

        createRequestDto = ProductCreateRequestDto.builder()
                .name("테스트 상품")
                .imageUrl("http://example.com/product.jpg")
                .price(10000)
                .stockQuantity(100)
                .categoryIds(List.of(1L, 2L))
                .build();

        updateRequestDto = ProductUpdateRequestDto.builder()
                .name("수정된 상품")
                .imageUrl("http://example.com/updated-product.jpg")
                .price(15000)
                .stockQuantity(50)
                .categoryIds(List.of(1L))
                .build();

        productDto = ProductDto.builder()
                .id(productId)
                .name("테스트 상품")
                .imageUrl("http://example.com/product.jpg")
                .price(10000)
                .stockQuantity(100)
                .build();

        product = Product.createProduct(
                "테스트 상품",
                "http://example.com/product.jpg",
                10000,
                100,
                List.of(),
                sellerUser
        );

        ReflectionTestUtils.setField(product, "id", productId);
        given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
    }

    @Test
    @DisplayName("상품 생성 성공")
    void createProduct_Success() throws Exception {
        // given
        given(productService.createProduct(any(User.class), any(ProductCreateRequestDto.class)))
                .willReturn(productDto);

        // when & then
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + sellerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                .andExpect(jsonPath("$.data.imageUrl").value("http://example.com/product.jpg"))
                .andExpect(jsonPath("$.data.price").value(10000))
                .andExpect(jsonPath("$.data.stockQuantity").value(100))
                .andDo(document("product-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (SELLER 또는 ADMIN 권한 필요)")
                        ),
                        requestFields(
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("imageUrl").description("상품 이미지 URL"),
                                fieldWithPath("price").description("상품 가격"),
                                fieldWithPath("stockQuantity").description("재고 수량"),
                                fieldWithPath("categoryIds").description("카테고리 ID 목록")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("상품 ID"),
                                fieldWithPath("data.name").description("상품 이름"),
                                fieldWithPath("data.imageUrl").description("상품 이미지 URL"),
                                fieldWithPath("data.price").description("상품 가격"),
                                fieldWithPath("data.stockQuantity").description("재고 수량")
                        )
                ));

        then(productService).should().createProduct(any(User.class), any(ProductCreateRequestDto.class));
    }

    @Test
    @DisplayName("상품 조회 성공")
    void getProduct_Success() throws Exception {
        // given
        given(productService.getProduct(any(User.class), eq(productId)))
                .willReturn(productDto);

        // when & then
        mockMvc.perform(get("/api/products/{productId}", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                .andExpect(jsonPath("$.data.imageUrl").value("http://example.com/product.jpg"))
                .andExpect(jsonPath("$.data.price").value(10000))
                .andExpect(jsonPath("$.data.stockQuantity").value(100))
                .andDo(document("product-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("상품 ID"),
                                fieldWithPath("data.name").description("상품 이름"),
                                fieldWithPath("data.imageUrl").description("상품 이미지 URL"),
                                fieldWithPath("data.price").description("상품 가격"),
                                fieldWithPath("data.stockQuantity").description("재고 수량")
                        )
                ));

        then(productService).should().getProduct(any(User.class), eq(productId));
    }

    @Test
    @DisplayName("상품 목록 조회 성공")
    void getProducts_Success() throws Exception {
        // given
        ProductDto productDto1 = ProductDto.builder()
                .id(1L)
                .name("상품1")
                .imageUrl("http://example.com/product1.jpg")
                .price(10000)
                .stockQuantity(100)
                .build();

        ProductDto productDto2 = ProductDto.builder()
                .id(2L)
                .name("상품2")
                .imageUrl("http://example.com/product2.jpg")
                .price(20000)
                .stockQuantity(50)
                .build();

        CursorPageResponseDto<ProductDto> pageResponse = CursorPageResponseDto.<ProductDto>builder()
                .content(List.of(productDto1, productDto2))
                .nextCursor("2025-01-01T00:00:00")
                .nextAfter(2L)
                .size(2)
                .hasNext(false)
                .build();

        given(productService.getProducts(
                any(User.class),
                eq("테스트"),
                eq("createdAt"),
                eq("ASC"),
                isNull(),
                isNull(),
                eq(10)
        )).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "테스트")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("product-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC/DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서 (페이징용)").optional(),
                                parameterWithName("after").description("기준 ID (페이징용)").optional(),
                                parameterWithName("limit").description("조회 개수 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content").description("상품 목록"),
                                fieldWithPath("data.content[].id").description("상품 ID"),
                                fieldWithPath("data.content[].name").description("상품 이름"),
                                fieldWithPath("data.content[].imageUrl").description("상품 이미지 URL"),
                                fieldWithPath("data.content[].price").description("상품 가격"),
                                fieldWithPath("data.content[].stockQuantity").description("재고 수량"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                fieldWithPath("data.nextCursor").description("다음 커서").optional(),
                                fieldWithPath("data.nextAfter").description("다음 기준 ID").optional(),
                                fieldWithPath("data.size").description("조회된 데이터 개수")
                        )
                ));

        then(productService).should().getProducts(
                any(User.class),
                eq("테스트"),
                eq("createdAt"),
                eq("ASC"),
                isNull(),
                isNull(),
                eq(10)
        );
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct_Success() throws Exception {
        // given
        ProductDto updatedProductDto = ProductDto.builder()
                .id(productId)
                .name("수정된 상품")
                .imageUrl("http://example.com/updated-product.jpg")
                .price(15000)
                .stockQuantity(50)
                .build();

        given(productService.updateProduct(any(User.class), eq(productId), any(ProductUpdateRequestDto.class)))
                .willReturn(updatedProductDto);

        // when & then
        mockMvc.perform(patch("/api/products/{productId}", productId)
                        .header("Authorization", "Bearer " + sellerAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("수정된 상품"))
                .andExpect(jsonPath("$.data.imageUrl").value("http://example.com/updated-product.jpg"))
                .andExpect(jsonPath("$.data.price").value(15000))
                .andExpect(jsonPath("$.data.stockQuantity").value(50))
                .andDo(document("product-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (SELLER 또는 ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("imageUrl").description("상품 이미지 URL"),
                                fieldWithPath("price").description("상품 가격"),
                                fieldWithPath("stockQuantity").description("재고 수량"),
                                fieldWithPath("categoryIds").description("카테고리 ID 목록")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("상품 ID"),
                                fieldWithPath("data.name").description("상품 이름"),
                                fieldWithPath("data.imageUrl").description("상품 이미지 URL"),
                                fieldWithPath("data.price").description("상품 가격"),
                                fieldWithPath("data.stockQuantity").description("재고 수량")
                        )
                ));

        then(productService).should().updateProduct(any(User.class), eq(productId), any(ProductUpdateRequestDto.class));
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_Success() throws Exception {
        // given
        willDoNothing().given(productService).deleteProduct(eq(productId));

        // when & then
        mockMvc.perform(delete("/api/products/{productId}", productId)
                        .header("Authorization", "Bearer " + sellerAccessToken))
                .andExpect(status().isNoContent())
                .andDo(document("product-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (SELLER 또는 ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID")
                        )
                ));

        then(productService).should().deleteProduct(eq(productId));
    }

    @Test
    @DisplayName("상품 북마크 성공")
    void bookmarkProduct_Success() throws Exception {
        // given
        ProductBookMarkDto bookmarkDto = ProductBookMarkDto.builder()
                .productId(productId)
                .userId(1L)
                .bookmarked(true)
                .build();

        given(productService.bookmarkProduct(any(User.class), eq(productId)))
                .willReturn(bookmarkDto);

        // when & then
        mockMvc.perform(post("/api/products/{productId}/bookmark", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.bookmarked").value(true))
                .andDo(document("product-bookmark",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        pathParameters(
                                parameterWithName("productId").description("상품 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.productId").description("상품 ID"),
                                fieldWithPath("data.userId").description("사용자 ID"),
                                fieldWithPath("data.bookmarked").description("북마크 상태")
                        )
                ));

        then(productService).should().bookmarkProduct(any(User.class), eq(productId));
    }
}
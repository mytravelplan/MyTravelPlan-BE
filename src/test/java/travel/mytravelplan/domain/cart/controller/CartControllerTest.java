package travel.mytravelplan.domain.cart.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.cart.dto.CartCreateRequestDto;
import travel.mytravelplan.domain.cart.dto.CartDto;
import travel.mytravelplan.domain.cart.dto.CartUpdateRequestDto;
import travel.mytravelplan.domain.cart.entity.Cart;
import travel.mytravelplan.domain.cart.service.CartService;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@DisplayName("장바구니 컨트롤러 테스트")
public class CartControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CartService cartService;

    private String accessToken;
    private User testUser;
    private Long cartId;
    private CartCreateRequestDto createRequestDto;
    private CartUpdateRequestDto updateRequestDto;
    private CartDto cartDto;
    private Cart cart;

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

        User seller = User.createUser(
                "sellerUser",
                "password",
                "",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "",
                Gender.FEMALE,
                Set.of(Role.SELLER)
        );

        testUser.setUserProfile(userProfile);

        ReflectionTestUtils.setField(testUser, "id", 1L);

        accessToken = jwtUtils.createAccessToken(1L, Set.of(Role.USER));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        cartId = 1L;

        createRequestDto = CartCreateRequestDto.builder()
                .productId(1L)
                .quantity(2)
                .build();

        updateRequestDto = CartUpdateRequestDto.builder()
                .quantity(5)
                .build();

        cartDto = CartDto.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .build();

        cart = Cart.createCart(
                Product.createProduct(
                        "테스트 상품",
                        "http://example.com/product.jpg",
                        10000,
                        50,
                        List.of(),
                        seller
                ),
                2
        );

        cart.setUser(testUser);
        ReflectionTestUtils.setField(cart, "id", cartId);
    }

    @Test
    @DisplayName("장바구니에 상품 담기 성공")
    void createCart_Success() throws Exception {
        // given
        given(cartService.createCart(any(User.class), any(CartCreateRequestDto.class)))
                .willReturn(cartDto);

        // when
        mockMvc.perform(post("/api/carts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.productId").value(1))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andDo(document("cart-add",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        requestFields(
                                fieldWithPath("productId").description("상품 ID"),
                                fieldWithPath("quantity").description("수량")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("장바구니 ID"),
                                fieldWithPath("data.productId").description("상품 ID"),
                                fieldWithPath("data.quantity").description("수량")
                        )
                ));

        // then
        then(cartService).should().createCart(any(User.class), any(CartCreateRequestDto.class));
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void getCarts_Success() throws Exception {
        // given
        CartDto cartDto1 = CartDto.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .build();

        CartDto cartDto2 = CartDto.builder()
                .id(2L)
                .productId(2L)
                .quantity(3)
                .build();

        List<CartDto> carts = List.of(cartDto1, cartDto2);

        given(cartService.getCarts(any(User.class))).willReturn(carts);

        // when
        mockMvc.perform(get("/api/carts")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[0].quantity").value(2))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].productId").value(2))
                .andExpect(jsonPath("$.data[1].quantity").value(3))
                .andDo(document("cart-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요)")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data").description("장바구니 목록"),
                                fieldWithPath("data[].id").description("장바구니 ID"),
                                fieldWithPath("data[].productId").description("상품 ID"),
                                fieldWithPath("data[].quantity").description("수량")
                        )
                ));

        // then
        then(cartService).should().getCarts(any(User.class));
    }

    @Test
    @DisplayName("장바구니 수량 수정 성공")
    void updateCart_Success() throws Exception {
        // given
        CartDto updatedCartDto = CartDto.builder()
                .id(1L)
                .productId(1L)
                .quantity(5)
                .build();

        given(cartRepository.findById(eq(cartId))).willReturn(Optional.of(cart));
        given(cartService.updateCart(eq(cartId), any(CartUpdateRequestDto.class)))
                .willReturn(updatedCartDto);

        // when
        mockMvc.perform(patch("/api/carts/{cartId}", cartId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.productId").value(1))
                .andExpect(jsonPath("$.data.quantity").value(5))
                .andDo(document("cart-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요, 권한 확인 필요)")
                        ),
                        pathParameters(
                                parameterWithName("cartId").description("장바구니 ID")
                        ),
                        requestFields(
                                fieldWithPath("quantity").description("수정할 수량")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("장바구니 ID"),
                                fieldWithPath("data.productId").description("상품 ID"),
                                fieldWithPath("data.quantity").description("수정된 수량")
                        )
                ));

        // then
        then(cartService).should().updateCart(eq(cartId), any(CartUpdateRequestDto.class));
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    void deleteCart_Success() throws Exception {
        // given
        given(cartRepository.findById(eq(cartId))).willReturn(Optional.of(cart));
        willDoNothing().given(cartService).deleteCart(eq(cartId));

        // when
        mockMvc.perform(delete("/api/carts/{cartId}", cartId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(document("cart-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰 (USER 또는 ADMIN 권한 필요, 권한 확인 필요)")
                        ),
                        pathParameters(
                                parameterWithName("cartId").description("장바구니 ID")
                        )
                ));

        // then
        then(cartService).should().deleteCart(eq(cartId));
    }
}

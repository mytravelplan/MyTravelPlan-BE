package travel.mytravelplan.domain.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import travel.mytravelplan.domain.cart.dto.CartCreateRequestDto;
import travel.mytravelplan.domain.cart.dto.CartDto;
import travel.mytravelplan.domain.cart.dto.CartUpdateRequestDto;
import travel.mytravelplan.domain.cart.entity.Cart;
import travel.mytravelplan.domain.cart.exception.CartException;
import travel.mytravelplan.domain.cart.mapper.CartMapper;
import travel.mytravelplan.domain.cart.repository.CartRepository;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.product.exception.ProductException;
import travel.mytravelplan.domain.product.repository.ProductRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@DisplayName("장바구니 서비스 테스트")
class CartServiceTest extends ServiceTestSupport {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;
    private CartDto cartDto;
    private CartCreateRequestDto createRequestDto;
    private CartUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        product = mock(Product.class);

        createRequestDto = CartCreateRequestDto.builder()
                .productId(1L)
                .quantity(2)
                .build();

        updateRequestDto = CartUpdateRequestDto.builder()
                .quantity(5)
                .build();

        cart = Cart.createCart(product, 2);

        cartDto = CartDto.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .build();
    }

    @Test
    @DisplayName("장바구니 생성 성공 - 새로운 상품")
    void createCart_Success_NewProduct() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(cartRepository.findByProductAndUser(eq(product), eq(user))).willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willReturn(cart);
        given(cartMapper.toDto(any(Cart.class))).willReturn(cartDto);

        // when
        CartDto result = cartService.createCart(user, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(cartDto);

        then(productRepository).should().findById(eq(1L));
        then(cartRepository).should().findByProductAndUser(eq(product), eq(user));
        then(cartRepository).should().save(any(Cart.class));
        then(cartMapper).should().toDto(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니 생성 성공 - 기존 상품 수량 업데이트")
    void createCart_Success_ExistingProduct() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.of(product));
        given(cartRepository.findByProductAndUser(eq(product), eq(user))).willReturn(Optional.of(cart));
        given(cartMapper.toDto(eq(cart))).willReturn(cartDto);

        // when
        CartDto result = cartService.createCart(user, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(cartDto);

        then(productRepository).should().findById(eq(1L));
        then(cartRepository).should().findByProductAndUser(eq(product), eq(user));
        then(cartMapper).should().toDto(eq(cart));
    }

    @Test
    @DisplayName("장바구니 생성 실패 - 상품을 찾을 수 없음")
    void createCart_Fail_ProductNotFound() {
        // given
        given(productRepository.findById(eq(1L))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.createCart(user, createRequestDto))
                .isInstanceOf(ProductException.class);

        then(productRepository).should().findById(eq(1L));
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void getCarts_Success() {
        // given
        Cart cart2 = Cart.createCart(product, 3);
        List<Cart> carts = List.of(cart, cart2);

        CartDto cartDto2 = CartDto.builder()
                .id(2L)
                .productId(1L)
                .quantity(3)
                .build();

        given(user.getCarts()).willReturn(carts);
        given(cartMapper.toDto(eq(cart))).willReturn(cartDto);
        given(cartMapper.toDto(eq(cart2))).willReturn(cartDto2);

        // when
        List<CartDto> result = cartService.getCarts(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        then(user).should().getCarts();
        then(cartMapper).should().toDto(eq(cart));
        then(cartMapper).should().toDto(eq(cart2));
    }

    @Test
    @DisplayName("장바구니 수정 성공")
    void updateCart_Success() {
        // given
        Long cartId = 1L;
        given(cartRepository.findById(eq(cartId))).willReturn(Optional.of(cart));
        given(cartMapper.toDto(eq(cart))).willReturn(cartDto);

        // when
        CartDto result = cartService.updateCart(cartId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(cartDto);

        then(cartRepository).should().findById(eq(cartId));
        then(cartMapper).should().toDto(eq(cart));
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 장바구니를 찾을 수 없음")
    void updateCart_Fail_CartNotFound() {
        // given
        Long cartId = 999L;
        given(cartRepository.findById(eq(cartId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.updateCart(cartId, updateRequestDto))
                .isInstanceOf(CartException.class);

        then(cartRepository).should().findById(eq(cartId));
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    void deleteCart_Success() {
        // given
        Long cartId = 1L;
        given(cartRepository.findById(eq(cartId))).willReturn(Optional.of(cart));

        // when
        cartService.deleteCart(cartId);

        // then
        then(cartRepository).should().findById(eq(cartId));
        then(cartRepository).should().delete(eq(cart));
    }

    @Test
    @DisplayName("장바구니 삭제 실패 - 장바구니를 찾을 수 없음")
    void deleteCart_Fail_CartNotFound() {
        // given
        Long cartId = 999L;
        given(cartRepository.findById(eq(cartId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.deleteCart(cartId))
                .isInstanceOf(CartException.class);

        then(cartRepository).should().findById(eq(cartId));
    }
}
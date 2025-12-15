package travel.mytravelplan.domain.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import travel.mytravelplan.global.error.code.CartErrorCode;
import travel.mytravelplan.global.error.code.ProductErrorCode;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartDto createCart(User currentUser, CartCreateRequestDto cartCreateRequest) {
        Product product = productRepository.findById(cartCreateRequest.getProductId())
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Optional<Cart> cartOptional = cartRepository.findByProductAndUser(product, currentUser);

        Cart cart;

        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
            cart.update(cartCreateRequest.getQuantity());
        } else {
            cart = Cart.createCart(product, cartCreateRequest.getQuantity());

            currentUser.addCart(cart);

            cartRepository.save(cart);
        }

        return cartMapper.toDto(cart);
    }

    public List<CartDto> getCarts(User currentUser) {
        return currentUser.getCarts().stream()
                .map(cartMapper::toDto)
                .toList();
    }

    @Transactional
    public CartDto updateCart(Long cartId, CartUpdateRequestDto cartUpdateRequestDto) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        cart.update(cartUpdateRequestDto.getQuantity());

        return cartMapper.toDto(cart);
    }

    @Transactional
    public void deleteCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        cartRepository.delete(cart);
    }
}

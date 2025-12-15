
package travel.mytravelplan.domain.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.cart.dto.CartCreateRequestDto;
import travel.mytravelplan.domain.cart.dto.CartDto;
import travel.mytravelplan.domain.cart.dto.CartUpdateRequestDto;
import travel.mytravelplan.domain.cart.service.CartService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    // 장바구니에 상품 담기
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CartDto>> createCart(@LoginUser User currentUser, @RequestBody @Validated CartCreateRequestDto cartCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(cartService.createCart(currentUser, cartCreateRequestDto)));
    }

    // 사용자의 장바구니에 담긴 상품 목록 조회
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CartDto>>> getCarts(@LoginUser User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCarts(currentUser)));
    }

    // 장바구니 상품 수정
    @PatchMapping("/{cartId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#cartId, 'Cart', 'cart:update')")
    public ResponseEntity<ApiResponse<CartDto>> updateCart(@PathVariable Long cartId, @RequestBody @Validated CartUpdateRequestDto cartUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateCart(cartId, cartUpdateRequestDto)));
     }

    // 특정 상품을 장바구니에서 삭제
    @DeleteMapping("/{cartId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#cartId, 'Cart', 'cart:delete')")
    public ResponseEntity<Void> deleteCart(@PathVariable Long cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.noContent().build();
    }
}

package travel.mytravelplan.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.product.dto.*;
import travel.mytravelplan.domain.product.entity.PopularProduct;
import travel.mytravelplan.domain.product.service.ProductService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // 상품 생성
    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @LoginUser User currentUser,
            @RequestBody @Validated ProductCreateRequestDto productCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(productService.createProduct(currentUser, productCreateRequestDto)));
    }

    // 상품 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@LoginUser User currentUser, @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProduct(currentUser, productId)));
    }

    // 상품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<ProductDto>>> getProducts(
            @LoginUser User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProducts(currentUser, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 상품 수정
    @PatchMapping("/{productId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN')) and hasPermission(#productId, 'Product', 'product:update')")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@LoginUser User currentUser, @PathVariable Long productId, @RequestBody @Validated ProductUpdateRequestDto productUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(currentUser, productId, productUpdateRequestDto)));
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN')) and hasPermission(#productId, 'Product', 'product:delete')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // 상품 북마크
    @PostMapping("/{productId}/bookmark")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductBookMarkDto>> bookmarkProduct(@LoginUser User currentUser, @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.bookmarkProduct(currentUser, productId)));
    }

/*
    // 인기 상품 목록 조회
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<PopularProductDto>>> getPopularProducts(
            @RequestParam(defaultValue = "DAILY", required = false) Period period,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(productService.getPopularProducts(period, direction, cursor, after, limit)));
    }
*/
}

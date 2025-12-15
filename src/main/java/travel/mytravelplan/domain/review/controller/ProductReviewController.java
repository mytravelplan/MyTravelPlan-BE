package travel.mytravelplan.domain.review.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.product.dto.PopularProductReviewDto;
import travel.mytravelplan.domain.review.dto.*;
import travel.mytravelplan.domain.review.service.ProductReviewService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products/{productId}/product-reviews")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    // 상품 리뷰 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductReviewDto>> createProductReview(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @RequestBody @Validated ProductReviewCreateRequestDto productReviewCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(productReviewService.createProductReview(currentUser, productId, productReviewCreateRequestDto)));
    }

    // 상품 리뷰 조회
    @GetMapping("/{productReviewId}")
    public ResponseEntity<ApiResponse<ProductReviewDto>> getProductReview(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @PathVariable Long productReviewId) {
        return ResponseEntity.ok(ApiResponse.success(productReviewService.getProductReview(currentUser, productId, productReviewId)));
    }


    // 상품 리뷰 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<ProductReviewDto>>> getProductReviews(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) boolean imgOnly,
            @RequestParam(required = false) BigDecimal rating,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(productReviewService.getProductReviews(currentUser, productId, keyword, imgOnly, rating, orderBy, direction, cursor, after, limit)));
    }

    // 상품 리뷰 수정
    @PatchMapping("/{productReviewId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#productReviewId,'ProductReview','productReview:update')")
    public ResponseEntity<ApiResponse<ProductReviewDto>> updateProductReview(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @PathVariable Long productReviewId,
            @RequestBody @Validated ProductReviewUpdateRequestDto productReviewUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(productReviewService.updateProductReview(currentUser, productId, productReviewId, productReviewUpdateRequestDto)));
    }

    // 상품 리뷰 삭제
    @DeleteMapping("/{productReviewId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#productReviewId,'ProductReview','productReview:delete')")
    public ResponseEntity<Void> deleteProductReview(@PathVariable Long productId, @PathVariable Long productReviewId) {
        productReviewService.deleteProductReview(productId, productReviewId);
        return ResponseEntity.noContent().build();
    }

    // 상품 리뷰 좋아요
    @PostMapping("/{productReviewId}/like")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductReviewLikeDto>> likeProductReview(@LoginUser User currentUser, @PathVariable Long productId, @PathVariable Long productReviewId) {
        return ResponseEntity.ok(ApiResponse.success(productReviewService.likeProductReview(currentUser, productId, productReviewId)));
    }

/*
    // 상품 인기 리뷰 조회
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<PopularProductReviewDto>>> getPopularReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "DAILY") Period period,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(productReviewService.getPopularProductReviews(productId, period, direction, cursor, after, limit)));
    }
*/
}

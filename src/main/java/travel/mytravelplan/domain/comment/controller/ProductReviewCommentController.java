package travel.mytravelplan.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentDto;
import travel.mytravelplan.domain.comment.dto.ProductReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.service.ProductReviewCommentService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/products/{productId}/product-reviews/{productReviewId}/product-review-comments")
@RequiredArgsConstructor
public class ProductReviewCommentController {
    private final ProductReviewCommentService productReviewCommentService;

    // 상품 리뷰 댓글 생성
    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductReviewCommentDto>> createProductReviewComment(@LoginUser User currentUser, @PathVariable Long productId, @PathVariable Long productReviewId, @RequestBody @Validated ProductReviewCommentCreateRequestDto productReviewCommentCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(productReviewCommentService.createProductReviewComment(currentUser, productId, productReviewId, productReviewCommentCreateRequestDto)));
    }

    // 상품 리뷰 댓글 조회
    @GetMapping("/{productReviewCommentId}")
    public ResponseEntity<ApiResponse<ProductReviewCommentDto>> getProductReviewComment(@PathVariable Long productId, @PathVariable Long productReviewId, @PathVariable Long productReviewCommentId) {
        return ResponseEntity.ok(ApiResponse.success(productReviewCommentService.getProductReviewComment(productId, productReviewId, productReviewCommentId)));
    }

    // 상품 리뷰 댓글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<ProductReviewCommentDto>>> getProductReviewComments(
            @PathVariable Long productId,
            @PathVariable Long productReviewId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(productReviewCommentService.getProductReviewComments(productId, productReviewId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 상품 리뷰 댓글 수정
    @PatchMapping("/{productReviewCommentId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN')) and hasPermission(#productReviewCommentId, 'ProductReviewComment', 'productReviewComment:update')")
    public ResponseEntity<ApiResponse<ProductReviewCommentDto>> updateProductReviewComment(@PathVariable Long productId, @PathVariable Long productReviewId, @PathVariable Long productReviewCommentId, @RequestBody @Validated ProductReviewCommentUpdateRequestDto productReviewCommentUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(productReviewCommentService.updateProductReviewComment(productId, productReviewId, productReviewCommentId, productReviewCommentUpdateRequestDto)));
    }

    // 상품 리뷰 댓글 삭제
    @DeleteMapping("/{productReviewCommentId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN')) and hasPermission(#productReviewCommentId, 'ProductReviewComment', 'productReviewComment:delete')")
    public ResponseEntity<Void> deleteProductReviewComment(@PathVariable Long productId, @PathVariable Long productReviewId, @PathVariable Long productReviewCommentId) {
        productReviewCommentService.deleteProductReviewComment(productId, productReviewId, productReviewCommentId);
        return ResponseEntity.noContent().build();
    }
}

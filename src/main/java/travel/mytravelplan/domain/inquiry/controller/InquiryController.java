package travel.mytravelplan.domain.inquiry.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.inquiry.service.InquiryService;
import travel.mytravelplan.domain.inquiry.dto.*;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/products/{productId}/inquiries")
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;

    // 상품 문의 등록
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryDto>> createProductInquiry(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @RequestBody @Validated InquiryCreateRequestDto inquiryCreateRequestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(inquiryService.createInquiry(currentUser, productId, inquiryCreateRequestDto)));
    }

    // 상품 문의 조회
    @GetMapping("/{inquiryId}")
    @PreAuthorize("hasPermission(#inquiryId,'Inquiry','inquiry:read')")
    public ResponseEntity<ApiResponse<InquiryDto>> getInquiry(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @PathVariable Long inquiryId) {
        return ResponseEntity.ok(ApiResponse.success(inquiryService.getInquiry(currentUser, productId, inquiryId)));
    }

    // 상품 문의 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<InquiryDto>>> getProductInquiries(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) boolean secretOnly,
            @RequestParam(required = false) boolean answerOnly,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(inquiryService.getInquiries(currentUser, productId, keyword, secretOnly, answerOnly, orderBy, direction, cursor, after, limit)));
    }

    // 상품 문의 수정
    @PatchMapping("/{inquiryId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#inquiryId,'Inquiry','inquiry:update')")
    public ResponseEntity<ApiResponse<InquiryDto>> updateInquiry(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @PathVariable Long inquiryId,
            @RequestBody @Validated InquiryUpdateRequestDto inquiryUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(inquiryService.updateInquiry(currentUser, productId, inquiryId, inquiryUpdateRequestDto)));
    }

    // 상품 문의 삭제
    @DeleteMapping("/{inquiryId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#inquiryId,'Inquiry','inquiry:delete')")
    public ResponseEntity<Void> deleteInquiry(
            @PathVariable Long productId,
            @PathVariable Long inquiryId) {
        inquiryService.deleteInquiry(productId, inquiryId);
        return ResponseEntity.noContent().build();
    }
}

package travel.mytravelplan.domain.inquiry.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyCreateRequestDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyDto;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyUpdateRequestDto;
import travel.mytravelplan.domain.inquiry.service.InquiryReplyService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/products/{productId}/inquiries/{inquiryId}/inquiry-replies")
@RequiredArgsConstructor
public class InquiryReplyController {
    private final InquiryReplyService inquiryReplyService;

    // 상품 문의 답변 생성
    @PostMapping
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<InquiryReplyDto>> createInquiryReply(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @PathVariable Long inquiryId,
            @RequestBody @Validated InquiryReplyCreateRequestDto inquiryReplyCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(inquiryReplyService.createInquiryReply(currentUser, productId, inquiryId, inquiryReplyCreateRequestDto)));
    }

    // 상품 문의 답변 조회
    @GetMapping("/{inquiryReplyId}")
    @PreAuthorize("hasPermission(#inquiryReplyId,'InquiryReply','inquiryReply:read')")
    public ResponseEntity<ApiResponse<InquiryReplyDto>> getInquiryReply(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @PathVariable Long inquiryId,
            @PathVariable Long inquiryReplyId
    ) {
        InquiryReplyDto inquiryReplyDto = inquiryReplyService.getInquiryReply(currentUser, productId, inquiryId, inquiryReplyId);
        return ResponseEntity.ok(ApiResponse.success(inquiryReplyDto));
    }

    // 상품 문의 답변 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<InquiryReplyDto>>> getInquiryReplies(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @PathVariable Long inquiryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(inquiryReplyService.getInquiryReplies(currentUser, productId, inquiryId, keyword, orderBy, direction, cursor, after, limit)));
    }


    // 상품 문의 답변 수정
    @PatchMapping("/{inquiryReplyId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN')) and hasPermission(#inquiryReplyId,'InquiryReply','inquiryReply:update')")
    public ResponseEntity<ApiResponse<InquiryReplyDto>> updateInquiryReply(
            @LoginUser User currentUser,
            @PathVariable Long productId,
            @PathVariable Long inquiryId,
            @PathVariable Long inquiryReplyId,
            @RequestBody @Validated InquiryReplyUpdateRequestDto inquiryReplyUpdateRequestDto
    ) {
        InquiryReplyDto updatedInquiryReply = inquiryReplyService.updateInquiryReply(currentUser, productId, inquiryId, inquiryReplyId, inquiryReplyUpdateRequestDto);
        return ResponseEntity.ok(ApiResponse.success(updatedInquiryReply));
    }

    // 상품 문의 답변 삭제
    @DeleteMapping("/{inquiryReplyId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN')) and hasPermission(#inquiryReplyId,'InquiryReply','inquiryReply:delete')")
    public ResponseEntity<Void> deleteInquiryReply(@PathVariable Long productId, @PathVariable Long inquiryId, @PathVariable Long inquiryReplyId) {
        inquiryReplyService.deleteInquiryReply(productId, inquiryId, inquiryReplyId);
        return ResponseEntity.noContent().build();
    }
}

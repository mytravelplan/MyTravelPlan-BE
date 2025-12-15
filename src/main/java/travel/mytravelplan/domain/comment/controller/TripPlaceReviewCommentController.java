package travel.mytravelplan.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentDto;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.service.TripPlaceReviewCommentService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/trip-places/{tripPlaceId}/trip-place-reviews/{tripPlaceReviewId}/trip-place-review-comments")
@RequiredArgsConstructor
public class TripPlaceReviewCommentController {
    private final TripPlaceReviewCommentService tripPlaceReviewCommentService;

    // 여행 장소 리뷰 댓글 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TripPlaceReviewCommentDto>> createTripPlaceReviewComment(@LoginUser User currentUser, @PathVariable Long tripPlaceId, @PathVariable Long tripPlaceReviewId, @RequestBody @Validated TripPlaceReviewCommentCreateRequestDto tripPlaceReviewCommentCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tripPlaceReviewCommentService.createTripPlaceReviewComment(currentUser, tripPlaceId, tripPlaceReviewId, tripPlaceReviewCommentCreateRequestDto)));
    }

    // 여행 장소 리뷰 댓글 조회
    @GetMapping("/{tripPlaceReviewCommentId}")
    public ResponseEntity<ApiResponse<TripPlaceReviewCommentDto>> getTripPlaceReviewComment(@PathVariable Long tripPlaceId, @PathVariable Long tripPlaceReviewId, @PathVariable Long tripPlaceReviewCommentId) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceReviewCommentService.getTripPlaceReviewComment(tripPlaceId, tripPlaceReviewId, tripPlaceReviewCommentId)));
    }

    // 여행 장소 리뷰 댓글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<TripPlaceReviewCommentDto>>> getTripPlaceReviewComments(
            @PathVariable Long tripPlaceId,
            @PathVariable Long tripPlaceReviewId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceReviewCommentService.getTripPlaceReviewComments(tripPlaceId, tripPlaceReviewId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 여행 장소 리뷰 댓글 수정
    @PatchMapping("/{tripPlaceReviewCommentId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripPlaceReviewCommentId, 'TripPlaceReviewComment', 'tripPlaceReviewComment:update')")
    public ResponseEntity<ApiResponse<TripPlaceReviewCommentDto>> updateTripPlaceReviewComment(@PathVariable Long tripPlaceId, @PathVariable Long tripPlaceReviewId, @PathVariable Long tripPlaceReviewCommentId, @RequestBody @Validated TripPlaceReviewCommentUpdateRequestDto tripPlaceReviewCommentUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceReviewCommentService.updateTripPlaceReviewComment(tripPlaceId, tripPlaceReviewId, tripPlaceReviewCommentId, tripPlaceReviewCommentUpdateRequestDto)));
    }

    // 여행 장소 리뷰 댓글 삭제
    @DeleteMapping("/{tripPlaceReviewCommentId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripPlaceReviewCommentId, 'TripPlaceReviewComment', 'tripPlaceReviewComment:delete')")
    public ResponseEntity<Void> deleteTripPlaceReviewComment(@PathVariable Long tripPlaceId, @PathVariable Long tripPlaceReviewId, @PathVariable Long tripPlaceReviewCommentId) {
        tripPlaceReviewCommentService.deleteTripPlaceReviewComment(tripPlaceId, tripPlaceReviewId, tripPlaceReviewCommentId);
        return ResponseEntity.noContent().build();
    }
}

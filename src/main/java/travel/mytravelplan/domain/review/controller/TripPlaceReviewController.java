package travel.mytravelplan.domain.review.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.review.dto.*;
import travel.mytravelplan.domain.review.service.TripPlaceReviewService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/trip-places/{tripPlaceId}/trip-place-reviews")
@RequiredArgsConstructor
public class TripPlaceReviewController {
    private final TripPlaceReviewService tripPlaceReviewService;

    // 여행 장소 리뷰 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TripPlaceReviewDto>> createTripPlaceReview(
            @LoginUser User currentUser,
            @PathVariable Long tripPlaceId,
            @RequestBody @Validated TripPlaceReviewCreateRequestDto tripPlaceReviewCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tripPlaceReviewService.createTripPlaceReview(currentUser, tripPlaceId, tripPlaceReviewCreateRequestDto)));
    }

    // 여행 장소 리뷰 조회
    @GetMapping("/{tripPlaceReviewId}")
    public ResponseEntity<ApiResponse<TripPlaceReviewDto>> getTripPlaceReview(
            @LoginUser User currentUser,
            @PathVariable Long tripPlaceId,
            @PathVariable Long tripPlaceReviewId) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceReviewService.getTripPlaceReview(currentUser, tripPlaceId, tripPlaceReviewId)));
    }

    // 여행 장소 리뷰 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<TripPlaceReviewDto>>> getTripPlaceReviews(
            @LoginUser User currentUser,
            @PathVariable Long tripPlaceId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) boolean imgOnly,
            @RequestParam(required = false) BigDecimal rating,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceReviewService.getTripPlaceReviews(currentUser, tripPlaceId, keyword, imgOnly, rating, orderBy, direction, cursor, after, limit)));
    }

    // 여행 장소 리뷰 수정
    @PatchMapping("/{tripPlaceReviewId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripPlaceReviewId,'TripPlaceReview','tripPlaceReview:update')")
    public ResponseEntity<ApiResponse<TripPlaceReviewDto>> updateTripPlaceReview(
            @LoginUser User currentUser,
            @PathVariable Long tripPlaceId,
            @PathVariable Long tripPlaceReviewId,
            @RequestBody @Validated TripPlaceReviewUpdateRequestDto tripPlaceReviewUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceReviewService.updateTripPlaceReview(currentUser, tripPlaceId, tripPlaceReviewId, tripPlaceReviewUpdateRequestDto)));
    }

    // 여행 장소 리뷰 삭제
    @DeleteMapping("/{tripPlaceReviewId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripPlaceReviewId, 'TripPlaceReview', 'tripPlaceReview:delete')")
    public ResponseEntity<Void> deleteTripPlaceReview(@PathVariable Long tripPlaceId, @PathVariable Long tripPlaceReviewId) {
        tripPlaceReviewService.deleteTripPlaceReview(tripPlaceId, tripPlaceReviewId);
        return ResponseEntity.noContent().build();
    }

    // 여행 장소 리뷰 좋아요
    @PostMapping("/{tripPlaceReviewId}/like")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TripPlaceReviewLikeDto>> likeTripPlaceReview(
            @LoginUser User currentUser,
            @PathVariable Long tripPlaceId,
            @PathVariable Long tripPlaceReviewId) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceReviewService.likeTripPlaceReview(currentUser, tripPlaceId, tripPlaceReviewId)));
    }

/*
    // 여행 장소 인기 리뷰 목록 조회
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<PopularTripPlaceReviewDto>>> getPopularTripPlaceReviews(
            @PathVariable Long tripPlaceId,
            @RequestParam(defaultValue = "DAILY") Period period,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceReviewService.getPopularTripPlaceReviews(tripPlaceId, period, direction, cursor, after, limit)));
    }
*/
}

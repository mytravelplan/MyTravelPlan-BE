package travel.mytravelplan.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.place.dto.*;
import travel.mytravelplan.domain.place.service.TripPlaceService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/trip-places")
@RequiredArgsConstructor
public class TripPlaceController {
    private final TripPlaceService tripPlaceService;

    // 여행 장소 생성
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TripPlaceDto>> createTripPlace(@LoginUser User currentUser, @RequestBody @Validated TripPlaceCreateRequestDto tripPlaceCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tripPlaceService.createTripPlace(currentUser, tripPlaceCreateRequestDto)));
    }

    // 여행 장소 조회
    @GetMapping("/{tripPlaceId}")
    public ResponseEntity<ApiResponse<TripPlaceDto>> getTripPlace(@LoginUser User currentUser, @PathVariable Long tripPlaceId) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceService.getTripPlace(currentUser, tripPlaceId)));
    }

    // 여행 장소 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponseDto<TripPlaceDto>>> getTripPlaces(
            @LoginUser User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceService.getTripPlaces(currentUser, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 여행 장소 수정
    @PatchMapping("/{tripPlaceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TripPlaceDto>> updateTripPlace(@LoginUser User currentUser, @PathVariable Long tripPlaceId, @RequestBody @Validated TripPlaceUpdateRequestDto tripPlaceUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceService.updateTripPlace(currentUser, tripPlaceId, tripPlaceUpdateRequestDto)));
    }

    // 여행 장소 삭제
    @DeleteMapping("/{tripPlaceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTripPlace(@PathVariable Long tripPlaceId) {
        tripPlaceService.deleteTripPlace(tripPlaceId);
        return ResponseEntity.noContent().build();
    }

    // 여행 장소 북마크
    @PostMapping("/{tripPlaceId}/bookmark")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<TripPlaceBookMarkDto>> bookmarkTripPlace(@LoginUser User currentUser, @PathVariable Long tripPlaceId) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceService.bookmarkTripPlace(currentUser, tripPlaceId)));
    }

/*
    // 인기 여행 장소 조회
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<PopularTripPlaceDto>>> getPopularTripPlaces(
            @RequestParam(defaultValue = "DAILY") Period period,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit) {
        return ResponseEntity.ok(ApiResponse.success(tripPlaceService.getPopularTripPlaces(period, direction, cursor, after, limit)));
    }
*/
}

package travel.mytravelplan.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.place.dto.*;
import travel.mytravelplan.domain.place.service.CustomPlaceService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/custom-places")
@RequiredArgsConstructor
public class CustomPlaceController {
    private final CustomPlaceService customPlaceService;

    // 나만의 장소 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomPlaceDto>> createCustomPlace(@LoginUser User currentUser, @RequestBody @Validated CustomPlaceCreateRequestDto customPlaceCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(customPlaceService.createCustomPlace(currentUser, customPlaceCreateRequestDto)));
    }

    // 나만의 장소 조회
    @GetMapping("/{customPlaceId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#customPlaceId, 'CustomPlace', 'customPlace:read')")
    public ResponseEntity<ApiResponse<CustomPlaceDto>> getCustomPlace(@PathVariable Long customPlaceId) {
        return ResponseEntity.ok(ApiResponse.success(customPlaceService.getCustomPlace(customPlaceId)));
    }

    // 나의 나만의 장소 목록 조회
    @GetMapping("/my-custom-places")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<CustomPlaceDto>>> getUserCustomPlaces(
            @LoginUser User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(customPlaceService.getCustomPlaces(currentUser, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 나만의 장소 수정
    @PatchMapping("/{customPlaceId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#customPlaceId, 'CustomPlace', 'customPlace:update')")
    public ResponseEntity<ApiResponse<CustomPlaceDto>> updateCustomPlace(@PathVariable Long customPlaceId, @RequestBody @Validated CustomPlaceUpdateRequestDto customPlaceUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(customPlaceService.updateCustomPlace(customPlaceId, customPlaceUpdateRequestDto)));
    }

    // 나만의 장소 삭제
    @DeleteMapping("/{customPlaceId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#customPlaceId, 'CustomPlace', 'customPlace:delete')")
    public ResponseEntity<Void> deleteCustomPlace(@PathVariable Long customPlaceId) {
        customPlaceService.deleteCustomPlace(customPlaceId);
        return ResponseEntity.noContent().build();
    }
}

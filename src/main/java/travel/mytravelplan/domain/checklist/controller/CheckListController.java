package travel.mytravelplan.domain.checklist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.checklist.dto.CheckListCreateRequestDto;
import travel.mytravelplan.domain.checklist.dto.CheckListDto;
import travel.mytravelplan.domain.checklist.dto.CheckListUpdateRequestDto;
import travel.mytravelplan.domain.checklist.service.CheckListService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/trips/{tripId}/checkLists")
@RequiredArgsConstructor
public class CheckListController {
    private final CheckListService checkListService;

    // 체크리스트 생성
    @PostMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkList:create')")
    public ResponseEntity<ApiResponse<CheckListDto>> createCheckList(@LoginUser User currentUser, @PathVariable Long tripId, @RequestBody @Validated CheckListCreateRequestDto checkListCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(checkListService.createCheckList(currentUser, tripId, checkListCreateRequestDto)));
    }

    // 체크리스트 조회
    @GetMapping("/{checkListId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkList:read')")
    public ResponseEntity<ApiResponse<CheckListDto>> getCheckList(@PathVariable Long tripId, @PathVariable Long checkListId) {
        return ResponseEntity.ok(ApiResponse.success(checkListService.getCheckList(tripId, checkListId)));
    }

    // 체크리스트 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkList:read')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<CheckListDto>>> getCheckLists(
            @PathVariable Long tripId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(checkListService.getCheckLists(tripId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 체크리스트 수정
    @PatchMapping("/{checkListId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkList:update')")
    public ResponseEntity<ApiResponse<CheckListDto>> updateCheckList(@PathVariable Long tripId, @PathVariable Long checkListId, @RequestBody @Validated CheckListUpdateRequestDto checkListUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(checkListService.updateCheckList(tripId, checkListId, checkListUpdateRequestDto)));
    }

    // 체크리스트 삭제
    @DeleteMapping("/{checkListId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkList:delete')")
    public ResponseEntity<Void> deleteCheckList(@PathVariable Long tripId, @PathVariable Long checkListId) {
        checkListService.deleteCheckList(tripId, checkListId);
        return ResponseEntity.noContent().build();
    }
}

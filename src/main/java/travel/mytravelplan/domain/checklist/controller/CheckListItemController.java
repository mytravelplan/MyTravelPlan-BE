package travel.mytravelplan.domain.checklist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.checklist.dto.CheckListItemCreateRequestDto;
import travel.mytravelplan.domain.checklist.dto.CheckListItemDto;
import travel.mytravelplan.domain.checklist.dto.CheckListItemUpdateRequestDto;
import travel.mytravelplan.domain.checklist.service.CheckListItemService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/trips/{tripId}/checkLists/{checkListId}/checkListItems")
@RequiredArgsConstructor
public class CheckListItemController {
    private final CheckListItemService checkListItemService;

    // 체크 리스트 항목 생성
    @PostMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkListItem:create')")
    public ResponseEntity<ApiResponse<CheckListItemDto>> createCheckListItem(@LoginUser User currentUser, @PathVariable Long tripId, @PathVariable Long checkListId, @RequestBody @Validated CheckListItemCreateRequestDto checkListItemCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(checkListItemService.createCheckListItem(currentUser, tripId, checkListId, checkListItemCreateRequestDto)));
    }

    // 체크리스트 항목 조회
    @GetMapping("/{checkListItemId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkListItem:read')")
    public ResponseEntity<ApiResponse<CheckListItemDto>> getCheckListItem(@PathVariable Long tripId, @PathVariable Long checkListId, @PathVariable Long checkListItemId) {
        return ResponseEntity.ok(ApiResponse.success(checkListItemService.getCheckListItem(tripId, checkListId, checkListItemId)));
    }

    // 체크 리스트 항목 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkListItem:read')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<? extends CheckListItemDto>>> getCheckListItems(
            @PathVariable Long tripId,
            @PathVariable Long checkListId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit) {
        return ResponseEntity.ok(ApiResponse.success(checkListItemService.getCheckListItems(tripId, checkListId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 체크리스트 항목 수정
    @PatchMapping("/{checkListItemId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkListItem:update') and hasPermission(#checkListId, 'CheckList', 'checkList:checkListItem:update')")
    public ResponseEntity<ApiResponse<CheckListItemDto>> updateCheckListItem(@PathVariable Long tripId, @PathVariable Long checkListId, @PathVariable Long checkListItemId, @RequestBody @Validated CheckListItemUpdateRequestDto checkListItemUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(checkListItemService.updateCheckListItem(tripId, checkListId, checkListItemId, checkListItemUpdateRequestDto)));
    }

    // 체크리스트 항목 삭제
    @DeleteMapping("/{checkListItemId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:checkListItem:delete') and hasPermission(#checkListId, 'CheckList', 'checkList:checkListItem:delete')")
    public ResponseEntity<Void> deleteCheckListItem(@PathVariable Long tripId, @PathVariable Long checkListId, @PathVariable Long checkListItemId) {
        checkListItemService.deleteCheckListItem(tripId, checkListId, checkListItemId);
        return ResponseEntity.noContent().build();
    }
}

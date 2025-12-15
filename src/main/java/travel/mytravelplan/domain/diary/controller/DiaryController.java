package travel.mytravelplan.domain.diary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.diary.dto.DiaryCreateRequestDto;
import travel.mytravelplan.domain.diary.dto.DiaryDto;
import travel.mytravelplan.domain.diary.dto.DiaryUpdateRequestDto;
import travel.mytravelplan.domain.diary.service.DiaryService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/trips/{tripId}/diaries")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    // 일기 생성
    @PostMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:diary:create')")
    public ResponseEntity<ApiResponse<DiaryDto>> createDiary(@LoginUser User currentUser, @PathVariable Long tripId, @RequestBody @Validated DiaryCreateRequestDto diaryCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(diaryService.createDiary(tripId, currentUser, diaryCreateRequestDto)));
    }

    // 일기 조회
    @GetMapping("/{diaryId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:diary:read')")
    public ResponseEntity<ApiResponse<DiaryDto>> getDiary(@PathVariable Long tripId, @PathVariable Long diaryId) {
        return ResponseEntity.ok(ApiResponse.success(diaryService.getDiary(tripId, diaryId)));
    }

    // 일기 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:diary:read')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<DiaryDto>>> getDiaries(
            @PathVariable Long tripId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit) {
        return ResponseEntity.ok(ApiResponse.success(diaryService.getDiaries(tripId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 일기 수정
    @PatchMapping("/{diaryId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:diary:update') and hasPermission(#diaryId, 'Diary', 'diary:update')")
    public ResponseEntity<ApiResponse<DiaryDto>> updateDiary(@PathVariable Long tripId, @PathVariable Long diaryId, @RequestBody @Validated DiaryUpdateRequestDto diaryUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(diaryService.updateDiary(tripId, diaryId, diaryUpdateRequestDto)));
    }

    // 일기 삭제
    @DeleteMapping("/{diaryId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:diary:delete') and hasPermission(#diaryId, 'Diary', 'diary:delete')")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long tripId, @PathVariable Long diaryId) {
        diaryService.deleteDiary(tripId, diaryId);
        return ResponseEntity.noContent().build();
    }
}

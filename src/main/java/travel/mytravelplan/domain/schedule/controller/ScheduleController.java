package travel.mytravelplan.domain.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.schedule.dto.ScheduleCreateRequestDto;
import travel.mytravelplan.domain.schedule.dto.ScheduleDto;
import travel.mytravelplan.domain.schedule.dto.ScheduleUpdateRequestDto;
import travel.mytravelplan.domain.schedule.service.ScheduleService;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/trips/{tripId}/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    // 일정 생성
    @PostMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:schedule:create')")
    public ResponseEntity<ApiResponse<ScheduleDto>> createSchedule(@PathVariable Long tripId, @RequestBody @Validated ScheduleCreateRequestDto scheduleCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(scheduleService.createSchedule(tripId, scheduleCreateRequestDto)));
    }

    // 일정 조회
    @GetMapping("/{scheduleId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:schedule:read')")
    public ResponseEntity<ApiResponse<ScheduleDto>> getSchedule(@PathVariable Long tripId, @PathVariable Long scheduleId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getSchedule(tripId, scheduleId)));
    }

    // 일정 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:schedule:read')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<ScheduleDto>>> getSchedules(
            @PathVariable Long tripId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getSchedules(tripId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 일정 수정
    @PatchMapping("/{scheduleId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:schedule:update')")
    public ResponseEntity<ApiResponse<ScheduleDto>> updateSchedule(@PathVariable Long tripId, @PathVariable Long scheduleId, @RequestBody @Validated ScheduleUpdateRequestDto scheduleUpdateRequestDto) {
        ScheduleDto updatedSchedule = scheduleService.updateSchedule(tripId, scheduleId, scheduleUpdateRequestDto);
        return ResponseEntity.ok(ApiResponse.success(updatedSchedule));
    }

    // 일정 삭제
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:schedule:delete')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long tripId, @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(tripId, scheduleId);
        return ResponseEntity.noContent().build();
    }
}

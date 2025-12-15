package travel.mytravelplan.domain.expense.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.expense.dto.ExpenseCreateRequestDto;
import travel.mytravelplan.domain.expense.dto.ExpenseDto;
import travel.mytravelplan.domain.expense.dto.ExpenseUpdateRequestDto;
import travel.mytravelplan.domain.expense.service.ExpenseService;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/trips/{tripId}/schedules/{scheduleId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    // 지출 생성
    @PostMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:expense:create')")
    public ResponseEntity<ApiResponse<ExpenseDto>> createExpense(
            @PathVariable Long tripId,
            @PathVariable Long scheduleId,
            @RequestBody @Validated ExpenseCreateRequestDto expenseCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(expenseService.createExpense(tripId, scheduleId, expenseCreateRequestDto)));
    }

    // 지출 조회
    @GetMapping("/{expenseId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:expense:read')")
    public ResponseEntity<ApiResponse<ExpenseDto>> getExpense(@PathVariable Long tripId, @PathVariable Long scheduleId, @PathVariable Long expenseId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpense(tripId, scheduleId, expenseId)));
    }

    // 지출 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:expense:read')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<ExpenseDto>>> getExpenses(
            @PathVariable Long tripId,
            @PathVariable Long scheduleId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpenses(tripId, scheduleId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 지출 수정
    @PatchMapping("/{expenseId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:expense:update')")
    public ResponseEntity<ApiResponse<ExpenseDto>> updateExpense(@PathVariable Long tripId, @PathVariable Long scheduleId, @PathVariable Long expenseId, @RequestBody @Validated ExpenseUpdateRequestDto expenseUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.updateExpense(tripId, scheduleId, expenseId, expenseUpdateRequestDto)));
    }

    // 지출 삭제
    @DeleteMapping("/{expenseId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:expense:delete')")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long tripId, @PathVariable Long scheduleId, @PathVariable Long expenseId) {
        expenseService.deleteExpense(tripId, scheduleId, expenseId);
        return ResponseEntity.noContent().build();
    }
}

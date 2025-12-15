package travel.mytravelplan.domain.budget.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.budget.dto.BudgetCreateRequestDto;
import travel.mytravelplan.domain.budget.dto.BudgetDto;
import travel.mytravelplan.domain.budget.dto.BudgetUpdateRequestDto;
import travel.mytravelplan.domain.budget.service.BudgetService;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/trips/{tripId}/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    // 예산 생성
    @PostMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:budget:create')")
    public ResponseEntity<ApiResponse<BudgetDto>> createBudget(@PathVariable Long tripId, @RequestBody @Validated BudgetCreateRequestDto budgetCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(budgetService.createBudget(tripId, budgetCreateRequestDto)));
    }

    // 예산 조회
    @GetMapping("/{budgetId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:budget:read')")
    public ResponseEntity<ApiResponse<BudgetDto>> getBudget(@PathVariable Long tripId, @PathVariable Long budgetId) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getBudget(tripId, budgetId)));
    }

    // 예산 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:budget:read')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<BudgetDto>>> getBudgets(
            @PathVariable Long tripId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getBudgets(tripId, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 예산 수정
    @PatchMapping("/{budgetId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:budget:update')")
    public ResponseEntity<ApiResponse<BudgetDto>> updateBudget(@PathVariable Long tripId, @PathVariable Long budgetId, @RequestBody @Validated BudgetUpdateRequestDto budgetUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.updateBudget(tripId, budgetId, budgetUpdateRequestDto)));
    }

    // 예산 삭제
    @DeleteMapping("/{budgetId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:budget:delete')")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long tripId, @PathVariable Long budgetId) {
        budgetService.deleteBudget(tripId, budgetId);
        return ResponseEntity.noContent().build();
    }
}

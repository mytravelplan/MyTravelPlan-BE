package travel.mytravelplan.domain.trip.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.expense.dto.*;
import travel.mytravelplan.domain.expense.enums.ExpenseType;
import travel.mytravelplan.domain.expense.service.ExpenseService;
import travel.mytravelplan.domain.trip.dto.*;
import travel.mytravelplan.domain.expense.enums.GroupByType;
import travel.mytravelplan.domain.trip.service.TripService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;
    private final ExpenseService expenseService;

    // 여행 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TripDto>> createTrip(@LoginUser User currentUser, @RequestBody @Validated TripCreateRequestDto tripCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tripService.createTrip(currentUser, tripCreateRequestDto)));
    }

    // 여행 조회
    @GetMapping("/{tripId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:read')")
    public ResponseEntity<ApiResponse<TripDto>> getTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getTrip(tripId)));
    }

    // 나의 여행 목록 조회
    @GetMapping("/my-trips")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN'))")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<TripDto>>> getUserTrips(
            @LoginUser User currentUser,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getUserTrips(currentUser, orderBy, direction, cursor, after, limit)));
    }

    // 여행 수정
    @PatchMapping("/{tripId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:update')")
    public ResponseEntity<ApiResponse<TripDto>> updateTrip(@PathVariable Long tripId, @RequestBody @Validated TripUpdateRequestDto tripUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(tripService.updateTrip(tripId, tripUpdateRequestDto)));
    }

    // 여행 삭제
    @DeleteMapping("/{tripId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:delete')")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long tripId) {
        tripService.deleteTrip(tripId);
        return ResponseEntity.noContent().build();
    }

    // 정산하기
    @GetMapping("/{tripId}/settlements")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:settle')")
    public ResponseEntity<ApiResponse<SettleExpenseDto>> settleExpenses(@PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.settleExpenses(tripId)));
    }

    // 지출 통계
    @GetMapping("/{tripId}/stats/expenses")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:stats')")
    public ResponseEntity<ApiResponse<ExpenseStatisticsDto>> getExpenseStatistics(
            @PathVariable Long tripId,
            @RequestParam ExpenseType expenseType,
            @RequestParam GroupByType groupBy,
            @RequestParam(required = false) LocalDate date

    ) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpenseStatistics(tripId, expenseType, groupBy, date)));
    }

    // 지출 CSV 다운로드
    @GetMapping("/{tripId}/expenses/export")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:exportExpenses')")
    public ResponseEntity<Resource> exportExpensesToExcel(@PathVariable Long tripId) {
        ByteArrayResource byteArrayResource = expenseService.exportExpensesToExcel(tripId);

        LocalDate now = LocalDate.now();

        String encodedFileName = URLEncoder.encode("가계부_" + now + ".csv", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"AccountBook_" + now + ".csv\"; filename*=UTF-8''" + encodedFileName)
                .body(byteArrayResource);
    }

/*
    // 여행 초대 링크 생성
    @PostMapping("/{tripId}/generate-invite-link")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:invite')")
    public ResponseEntity<ApiResponse<TripInviteLinkDto>> generateTripInviteLink(@PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.success(tripService.generateTripInviteLink(tripId)));
    }
*/

/*
    // 여행 초대 링크로 여행 참가
    @PostMapping("/join")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TripDto>> join(@LoginUser User user, @RequestBody @Validated TripJoinRequestDto tripJoinRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(tripService.join(user, tripJoinRequestDto)));
    }
*/
}
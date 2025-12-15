package travel.mytravelplan.domain.currency.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.currency.dto.TripCurrencyUpdateRequestDto;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.service.TripCurrencyService;
import travel.mytravelplan.domain.currency.dto.TripCurrencyCreateRequestDto;
import travel.mytravelplan.domain.currency.dto.TripCurrencyDto;
import travel.mytravelplan.global.common.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/trip-currencies")
@RequiredArgsConstructor
public class TripCurrencyController {
    private final TripCurrencyService tripCurrencyService;

    // 여행 통화 생성
    @PostMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:tripCurrency:create')")
    public ResponseEntity<ApiResponse<TripCurrencyDto>> createTripCurrency(@PathVariable Long tripId, @RequestBody @Validated TripCurrencyCreateRequestDto tripCurrencyCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tripCurrencyService.createTripCurrency(tripId, tripCurrencyCreateRequestDto)));
    }

    // 여행 통화 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:tripCurrency:read')")
    public ResponseEntity<ApiResponse<List<TripCurrencyDto>>> getTripCurrencies(@PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.success(tripCurrencyService.getTripCurrencies(tripId)));
    }

    // 여행 통화 수정
    @PatchMapping("/{currencyType}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#tripId, 'Trip', 'trip:tripCurrency:update')")
    public ResponseEntity<ApiResponse<TripCurrencyDto>> updateTripCurrency(
            @PathVariable Long tripId,
            @PathVariable CurrencyType currencyType,
            @RequestBody @Validated TripCurrencyUpdateRequestDto tripCurrencyUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(tripCurrencyService.updateTripCurrency(tripId, currencyType, tripCurrencyUpdateRequestDto)));
    }
}

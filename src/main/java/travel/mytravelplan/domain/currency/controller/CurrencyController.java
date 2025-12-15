package travel.mytravelplan.domain.currency.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.mytravelplan.domain.currency.dto.CurrencyDto;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import travel.mytravelplan.domain.currency.service.CurrencyService;
import travel.mytravelplan.global.common.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/currencies")
public class CurrencyController {
    private final CurrencyService currencyService;

    // 통화 조회
    @GetMapping("/{currencyType}")
    public ResponseEntity<ApiResponse<CurrencyDto>> getCurrency(@PathVariable CurrencyType currencyType) {
        return ResponseEntity.ok(ApiResponse.success(currencyService.getCurrency(currencyType)));
    }

    // 통화 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CurrencyDto>>> getAllCurrencies() {
        return ResponseEntity.ok(ApiResponse.success(currencyService.getAllCurrencies()));
    }
}

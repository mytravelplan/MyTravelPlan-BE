package travel.mytravelplan.domain.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.delivery.dto.DeliveryDto;
import travel.mytravelplan.domain.delivery.dto.DeliveryUpdateRequestDto;
import travel.mytravelplan.domain.delivery.service.DeliveryService;
import travel.mytravelplan.global.common.response.ApiResponse;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    // 배송 조회
    @GetMapping("/{deliveryId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#deliveryId, 'Delivery', 'delivery:read')")
    public ResponseEntity<ApiResponse<DeliveryDto>> getDelivery(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getDelivery(deliveryId)));
    }

    // 배송 정보 수정
    @PatchMapping("/{deliveryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateDelivery(@PathVariable Long deliveryId, @RequestBody @Validated DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.updateDelivery(deliveryId, deliveryUpdateRequestDto)));
    }
}

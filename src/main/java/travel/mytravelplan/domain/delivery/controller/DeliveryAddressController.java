package travel.mytravelplan.domain.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressCreateRequestDto;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressDto;
import travel.mytravelplan.domain.delivery.service.DeliveryAddressService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-addresses")
@RequiredArgsConstructor
public class DeliveryAddressController {
    private final DeliveryAddressService deliveryAddressService;

    // 배송지 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryAddressDto>> createDeliveryAddress(@LoginUser User currentUser, @RequestBody @Validated DeliveryAddressCreateRequestDto deliveryAddressCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(deliveryAddressService.createDeliveryAddress(currentUser, deliveryAddressCreateRequestDto)));
    }

    // 배송지 목록 조회
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DeliveryAddressDto>>> getAllDeliveryAddresses(
            @LoginUser User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(deliveryAddressService.getAllDeliveryAddresses(currentUser)));
    }
}

package travel.mytravelplan.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.order.dto.OrderCreateRequestDto;
import travel.mytravelplan.domain.order.dto.OrderDto;
import travel.mytravelplan.domain.order.enums.OrderStatus;
import travel.mytravelplan.domain.order.service.OrderService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(@LoginUser User currentUser, @RequestBody @Validated OrderCreateRequestDto orderCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(orderService.createOrder(currentUser, orderCreateRequestDto)));
    }

    // 주문 조회
    @GetMapping("/{orderId}")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#orderId, 'Order', 'order:read')")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrder(orderId)));
    }

    // 주문 목록 조회
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<OrderDto>>> getOrders(
            @LoginUser User currentUser,
            @RequestParam(defaultValue = "ORDER", required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrders(currentUser, orderStatus, orderBy, direction, cursor, after, limit)));
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#orderId, 'Order', 'order:cancel')")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(orderId)));
    }
}

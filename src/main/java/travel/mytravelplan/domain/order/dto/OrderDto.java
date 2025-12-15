package travel.mytravelplan.domain.order.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressDto;
import java.time.LocalDateTime;

@Getter
public class OrderDto {
    private Long orderId; // 주문 번호

    private LocalDateTime orderDate; // 주문 날짜

    private int totalPrice; // 총 주문 금액

    private LocalDateTime paymentCompletedAt; // 결제 완료 시간

    private OrdererDto orderer; // 주문자 정보

    private DeliveryAddressDto deliveryAddress; // 배송지 정보

    @Builder
    private OrderDto(Long orderId, LocalDateTime orderDate, int totalPrice,
                     LocalDateTime paymentCompletedAt, OrdererDto orderer,
                     DeliveryAddressDto deliveryAddress) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.paymentCompletedAt = paymentCompletedAt;
        this.orderer = orderer;
        this.deliveryAddress = deliveryAddress;
    }
}

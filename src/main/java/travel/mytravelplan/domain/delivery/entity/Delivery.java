package travel.mytravelplan.domain.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.enums.DeliveryStatus;
import travel.mytravelplan.domain.order.entity.Order;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Address addresses;

    private String requirement; // 배송 요청 사항;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus; // 배송 상태 (예: READY, DELIVERING, COMP)

    @Setter
    @OneToOne(mappedBy = "delivery")
    private Order order;

    @Builder(access = AccessLevel.PRIVATE)
    private Delivery(Address addresses, String requirement, DeliveryStatus deliveryStatus) {
        this.addresses = addresses;
        this.requirement = requirement;
        this.deliveryStatus = deliveryStatus;
    }

    public static Delivery createDelivery(Address addresses, String requirement) {
        return Delivery.builder()
                .addresses(addresses)
                .requirement(requirement)
                .deliveryStatus(DeliveryStatus.READY)
                .build();
    }

    public void update(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}

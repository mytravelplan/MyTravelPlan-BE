package travel.mytravelplan.domain.delivery.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryAddress extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Address address;

    private boolean defaultDeliveryAddress; // 기본 배송지 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private DeliveryAddress(Address address, boolean defaultDeliveryAddress, User user) {
        this.address = address;
        this.defaultDeliveryAddress = defaultDeliveryAddress;
        this.user = user;
    }

    public static DeliveryAddress createDeliveryAddress(Address address, boolean defaultDeliveryAddress, User user) {
        return DeliveryAddress.builder()
                .address(address)
                .defaultDeliveryAddress(defaultDeliveryAddress)
                .user(user)
                .build();
    }
}

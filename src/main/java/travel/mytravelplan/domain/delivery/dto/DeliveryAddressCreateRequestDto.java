package travel.mytravelplan.domain.delivery.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.delivery.enums.Address;

@Getter
@NoArgsConstructor
public class DeliveryAddressCreateRequestDto {
    private Address address;
    private boolean defaultDeliveryAddress; // 기본 배송지 여부

    @Builder
    private DeliveryAddressCreateRequestDto(Address address, boolean defaultDeliveryAddress) {
        this.address = address;
        this.defaultDeliveryAddress = defaultDeliveryAddress;
    }
}

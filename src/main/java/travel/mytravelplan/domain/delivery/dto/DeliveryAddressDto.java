package travel.mytravelplan.domain.delivery.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.delivery.enums.Address;

@Getter
public class DeliveryAddressDto {
    private Long id;
    private Address address;

    @Builder
    private DeliveryAddressDto(Long id, Address address) {
        this.id = id;
        this.address = address;
    }
}

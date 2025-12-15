package travel.mytravelplan.domain.delivery.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.delivery.enums.Address;
import travel.mytravelplan.domain.delivery.enums.DeliveryStatus;

@Getter
public class DeliveryDto {
    private Long id;
    private Address address;
    private DeliveryStatus deliveryStatus;
    private String requirement; // 배송 요청 사항;

    @Builder
    private DeliveryDto(Long id, Address address, DeliveryStatus deliveryStatus, String requirement) {
        this.id = id;
        this.address = address;
        this.deliveryStatus = deliveryStatus;
        this.requirement = requirement;
    }
}

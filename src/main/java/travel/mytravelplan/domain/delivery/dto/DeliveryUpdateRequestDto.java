package travel.mytravelplan.domain.delivery.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.delivery.enums.DeliveryStatus;

@Getter
@NoArgsConstructor
public class DeliveryUpdateRequestDto {
    private DeliveryStatus deliveryStatus;

    @Builder
    private DeliveryUpdateRequestDto(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}

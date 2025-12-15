package travel.mytravelplan.domain.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.order.enums.Orderer;

import java.util.Map;

@Getter
@NoArgsConstructor
public class OrderCreateRequestDto {
    private Map<Long, Integer> products;
    private Orderer orderer;
    private Long deliveryAddressId;
    private String requirement;

    @Builder
    private OrderCreateRequestDto(Map<Long, Integer> products, Orderer orderer, Long deliveryAddressId, String requirement) {
        this.products = products;
        this.orderer = orderer;
        this.deliveryAddressId = deliveryAddressId;
        this.requirement = requirement;
    }
}

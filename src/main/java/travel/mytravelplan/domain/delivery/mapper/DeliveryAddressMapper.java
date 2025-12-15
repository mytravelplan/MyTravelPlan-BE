package travel.mytravelplan.domain.delivery.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.delivery.dto.DeliveryAddressDto;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;

@Mapper(componentModel = "spring")
public interface DeliveryAddressMapper {
    DeliveryAddressDto toDto(DeliveryAddress deliveryAddress);
}

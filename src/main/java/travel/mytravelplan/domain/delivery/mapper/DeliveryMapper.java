package travel.mytravelplan.domain.delivery.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.delivery.dto.DeliveryDto;
import travel.mytravelplan.domain.delivery.entity.Delivery;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    DeliveryDto toDto(Delivery delivery);
}

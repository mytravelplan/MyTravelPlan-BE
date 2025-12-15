package travel.mytravelplan.domain.order.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.order.dto.OrderDto;
import travel.mytravelplan.domain.order.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDto toDto(Order order);
}

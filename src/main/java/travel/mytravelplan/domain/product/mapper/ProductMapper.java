package travel.mytravelplan.domain.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.product.dto.ProductDto;
import travel.mytravelplan.domain.product.entity.Product;
import travel.mytravelplan.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", source = "product.id")
    ProductDto toDto(Product product, User user);
}

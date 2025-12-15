package travel.mytravelplan.domain.cart.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.cart.dto.CartDto;
import travel.mytravelplan.domain.cart.entity.Cart;
import travel.mytravelplan.domain.post.dto.PostDto;
import travel.mytravelplan.domain.post.entity.Post;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "productId", source = "product.id")
    CartDto toDto(Cart cart);
}

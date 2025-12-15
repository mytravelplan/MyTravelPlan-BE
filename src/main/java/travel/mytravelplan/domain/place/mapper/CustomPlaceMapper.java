package travel.mytravelplan.domain.place.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.place.dto.CustomPlaceDto;
import travel.mytravelplan.domain.place.entity.CustomPlace;

@Mapper(componentModel = "spring")
public interface CustomPlaceMapper {
    CustomPlaceDto toDto(CustomPlace customPlace);
}

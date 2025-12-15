package travel.mytravelplan.domain.place.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.place.dto.TripPlaceDto;
import travel.mytravelplan.domain.place.entity.Place;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface TripPlaceMapper {
    @Mapping(target = "id", source = "tripPlace.id")
    TripPlaceDto toDto(TripPlace tripPlace, User user);
}

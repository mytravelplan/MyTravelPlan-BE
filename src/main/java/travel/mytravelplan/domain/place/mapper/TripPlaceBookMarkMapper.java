package travel.mytravelplan.domain.place.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.place.dto.TripPlaceBookMarkDto;
import travel.mytravelplan.domain.place.entity.TripPlaceBookMark;

@Mapper(componentModel = "spring")
public interface TripPlaceBookMarkMapper {
    @Mapping(source = "tripPlaceBookMark.tripPlace.id", target = "tripPlaceId")
    @Mapping(source = "tripPlaceBookMark.user.id", target = "userId")
    TripPlaceBookMarkDto toDto(TripPlaceBookMark tripPlaceBookMark, boolean bookmarked);
}

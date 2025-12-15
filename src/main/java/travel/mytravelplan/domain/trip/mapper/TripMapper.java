package travel.mytravelplan.domain.trip.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.trip.dto.TripDto;
import travel.mytravelplan.domain.trip.entity.Trip;

@Mapper(componentModel = "spring")
public interface TripMapper {
    TripDto toDto(Trip trip);
}
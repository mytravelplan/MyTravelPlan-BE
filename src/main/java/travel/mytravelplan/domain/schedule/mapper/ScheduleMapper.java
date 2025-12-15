package travel.mytravelplan.domain.schedule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.schedule.dto.ScheduleDto;
import travel.mytravelplan.domain.schedule.entity.Schedule;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "tripId", source = "schedule.trip.id")
    @Mapping(target = "placeName", source = "schedule.place.name")
    ScheduleDto toDto(Schedule schedule);
}

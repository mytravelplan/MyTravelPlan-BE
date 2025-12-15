package travel.mytravelplan.domain.auth.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.auth.dto.JwtTokenDto;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    JwtTokenDto toDto(String accessToken);
}
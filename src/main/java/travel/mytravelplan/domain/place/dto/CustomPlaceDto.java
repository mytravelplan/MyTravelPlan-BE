package travel.mytravelplan.domain.place.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CustomPlaceDto extends PlaceDto {

    @Builder
    private CustomPlaceDto(Long id, String name, String address, String description, BigDecimal longitude, BigDecimal latitude) {
        super(id, name, address, description, longitude, latitude);
    }
}

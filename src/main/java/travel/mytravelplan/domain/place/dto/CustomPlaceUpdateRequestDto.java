package travel.mytravelplan.domain.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.enums.PlaceType;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CustomPlaceUpdateRequestDto extends PlaceUpdateRequestDto {

    @Builder
    private CustomPlaceUpdateRequestDto(PlaceType placeType, String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category) {
        super(placeType, name, address, description, latitude, longitude, category);
    }
}

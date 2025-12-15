package travel.mytravelplan.domain.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.enums.PlaceType;

@Getter
@NoArgsConstructor
public class TripPlaceUpdateRequestDto extends PlaceUpdateRequestDto {
    private String externalUrl;

    @Builder
    private TripPlaceUpdateRequestDto(PlaceType placeType, String name, String address, String description,
                                      java.math.BigDecimal latitude, java.math.BigDecimal longitude, PlaceCategory category, String externalUrl) {
        super(placeType, name, address, description, latitude, longitude, category);
        this.externalUrl = externalUrl;
    }
}

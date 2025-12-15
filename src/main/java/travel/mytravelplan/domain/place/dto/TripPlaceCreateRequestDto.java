package travel.mytravelplan.domain.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.enums.PlaceType;

@Getter
@NoArgsConstructor
public class TripPlaceCreateRequestDto extends PlaceCreateRequestDto {
    private String externalUrl;

    @Builder
    private TripPlaceCreateRequestDto(PlaceType placeType, String name, String address, String description,
                                      java.math.BigDecimal latitude, java.math.BigDecimal longitude,
                                      travel.mytravelplan.domain.place.enums.PlaceCategory category,
                                      String externalUrl) {
        super(placeType, name, address, description, latitude, longitude, category);
        this.externalUrl = externalUrl;
    }
}

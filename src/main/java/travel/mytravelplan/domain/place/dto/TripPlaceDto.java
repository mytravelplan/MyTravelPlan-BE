package travel.mytravelplan.domain.place.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class TripPlaceDto extends PlaceDto {
    private String externalUrl;

    @Builder
    private TripPlaceDto(Long id, String name, String address, String description,
                         BigDecimal longitude, BigDecimal latitude,
                         String externalUrl) {
        super(id, name, address, description, longitude, latitude);
        this.externalUrl = externalUrl;
    }
}

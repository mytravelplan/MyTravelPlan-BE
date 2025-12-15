package travel.mytravelplan.domain.place.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.enums.PlaceCategory;

import java.math.BigDecimal;

@Getter
@Entity
@DiscriminatorValue("TRIP")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPlace extends Place {
    private String externalUrl; // 외부 장소 URL

    @Builder(access = AccessLevel.PRIVATE)
    private TripPlace(String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category, String externalUrl) {
        super(name, address, description, latitude, longitude, category);
        this.externalUrl = externalUrl;
    }

    public static TripPlace createTripPlace(String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category, String externalUrl) {
        return TripPlace.builder()
                .name(name)
                .address(address)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .category(category)
                .externalUrl(externalUrl)
                .build();
    }

    public void update(String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category, String externalUrl) {
        super.update(name, address, description, latitude, longitude, category);
        this.externalUrl = externalUrl;
    }
}

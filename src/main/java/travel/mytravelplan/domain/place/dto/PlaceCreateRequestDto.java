package travel.mytravelplan.domain.place.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.place.enums.PlaceType;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "placeType",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CustomPlaceCreateRequestDto.class, name = "CUSTOM"),
        @JsonSubTypes.Type(value = TripPlaceCreateRequestDto.class, name = "TRIP"),
})
public abstract class PlaceCreateRequestDto {
    private PlaceType placeType;
    private String name;
    private String address;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private PlaceCategory category;

    protected PlaceCreateRequestDto(PlaceType placeType, String name, String address, String description,
                                    BigDecimal latitude, BigDecimal longitude, PlaceCategory category) {
        this.placeType = placeType;
        this.name = name;
        this.address = address;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }
}

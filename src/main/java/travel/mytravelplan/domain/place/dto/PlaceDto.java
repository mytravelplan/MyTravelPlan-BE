package travel.mytravelplan.domain.place.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public abstract class PlaceDto {
    private Long id;
    private String name;
    private String address;
    private String description;
    private BigDecimal longitude;
    private BigDecimal latitude;

    protected PlaceDto(Long id, String name, String address, String description, BigDecimal longitude, BigDecimal latitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}

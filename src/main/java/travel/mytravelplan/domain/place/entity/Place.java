package travel.mytravelplan.domain.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Place extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private String description;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    private PlaceCategory category;

    protected Place(String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }

    protected void update(String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }
}

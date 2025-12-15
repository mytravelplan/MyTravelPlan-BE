package travel.mytravelplan.domain.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.user.entity.User;

import java.math.BigDecimal;

@Getter
@Entity
@DiscriminatorValue("CUSTOM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomPlace extends Place {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private CustomPlace(String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category, User user) {
        super(name, address, description, latitude, longitude, category);
        this.user = user;
    }

    public static CustomPlace createCustomPlace(String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category, User user) {
        return CustomPlace.builder()
                .name(name)
                .address(address)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .category(category)
                .user(user)
                .build();
    }

    public void update(String name, String address, String description, BigDecimal latitude, BigDecimal longitude, PlaceCategory category) {
        super.update(name, address, description, latitude, longitude, category);
    }
}

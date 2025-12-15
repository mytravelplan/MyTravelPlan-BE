package travel.mytravelplan.domain.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPlaceBookMark extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_place_id")
    private TripPlace tripPlace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private TripPlaceBookMark(TripPlace tripPlace, User user) {
        this.tripPlace = tripPlace;
        this.user = user;
    }

    public static TripPlaceBookMark createTripPlaceBookMark(TripPlace tripPlace, User user) {
        return TripPlaceBookMark.builder()
                .tripPlace(tripPlace)
                .user(user)
                .build();
    }
}

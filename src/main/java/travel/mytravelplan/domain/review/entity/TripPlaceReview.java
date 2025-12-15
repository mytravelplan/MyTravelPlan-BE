package travel.mytravelplan.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPlaceReview extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private TripPlace tripPlace;

    @ElementCollection
    @CollectionTable(
            name = "trip_place_review_image_url",
            joinColumns = @JoinColumn(name = "trip_place_review_id")
    )
    private List<String> imageUrls = new ArrayList<>();

    private String content;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Builder(access = AccessLevel.PRIVATE)
    private TripPlaceReview(User user, TripPlace tripPlace, BigDecimal rating, String content) {
        this.user = user;
        this.tripPlace = tripPlace;
        this.rating = rating;
        this.content = content;
    }

    public static TripPlaceReview createTripPlaceReview(User user, TripPlace tripPlace, BigDecimal rating, String content) {
        return TripPlaceReview.builder()
                .user(user)
                .tripPlace(tripPlace)
                .rating(rating)
                .content(content)
                .build();
    }

    public void update(String content, BigDecimal rating) {
        this.content = content;
        this.rating = rating;
    }
}
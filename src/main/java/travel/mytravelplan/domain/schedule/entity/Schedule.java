package travel.mytravelplan.domain.schedule.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.place.entity.Place;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String memo;

    private Long displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private BigDecimal rating;

    @Builder(access = AccessLevel.PRIVATE)
    private Schedule(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, String memo, Long displayOrder, Place place, Trip trip, BigDecimal rating) {
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.memo = memo;
        this.displayOrder = displayOrder;
        this.place = place;
        this.trip = trip;
        this.rating = rating;
    }

    public static Schedule createSchedule(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, String memo, Long displayOrder, Place place, Trip trip, BigDecimal rating) {
        return Schedule.builder()
                .title(title)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .memo(memo)
                .displayOrder(displayOrder)
                .place(place)
                .trip(trip)
                .rating(rating)
                .build();
    }

    public void update(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, String memo, BigDecimal rating, Place place) {
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.memo = memo;
        this.rating = rating;
        this.place = place;
    }
}

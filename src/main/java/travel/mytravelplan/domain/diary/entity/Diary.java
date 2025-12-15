package travel.mytravelplan.domain.diary.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.diary.enums.Emotion;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private List<String> imageUrls = new ArrayList<>();

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private Emotion emotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_join_id")
    private TripJoin tripJoin;

    @Builder(access = AccessLevel.PRIVATE)
    private Diary(String title, String content, List<String> imageUrls, LocalDate date, Emotion emotion, Trip trip, TripJoin tripJoin) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
        this.date = date;
        this.emotion = emotion;
        this.trip = trip;
        this.tripJoin = tripJoin;
    }

    public static Diary createDiary(String title, String content, List<String> imageUrls, LocalDate date, Emotion emotion, Trip trip, TripJoin tripJoin) {
        return Diary.builder()
                .title(title)
                .content(content)
                .imageUrls(imageUrls)
                .date(date)
                .emotion(emotion)
                .trip(trip)
                .tripJoin(tripJoin)
                .build();
    }

    public void update(String title, String content, List<String> imageUrls, LocalDate date, Emotion emotion) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
        this.date = date;
        this.emotion = emotion;
    }
}

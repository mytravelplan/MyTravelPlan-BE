package travel.mytravelplan.domain.trip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.trip.dto.TripUpdateRequestDto;
import travel.mytravelplan.domain.trip.enums.Country;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trip extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    private String imageUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "trip_countries", joinColumns = @JoinColumn(name = "trip_id"))
    @Column(name = "country")
    @Enumerated(EnumType.STRING)
    private Set<Country> countries = new HashSet<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Trip(String title, LocalDate startDate, LocalDate endDate, String imageUrl, Set<Country> countries) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.countries = countries;
    }

    public static Trip createTrip(String title, LocalDate startDate, LocalDate endDate, String imageUrl, Set<Country> countries) {
        return Trip.builder()
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .imageUrl(imageUrl)
                .countries(countries)
                .build();
    }

    public void update(String title, LocalDate startDate, LocalDate endDate, String imageUrl, Set<Country> countries) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.countries = countries;
    }
}

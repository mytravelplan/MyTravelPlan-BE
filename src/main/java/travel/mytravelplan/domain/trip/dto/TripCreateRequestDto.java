package travel.mytravelplan.domain.trip.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.trip.enums.Country;

import java.time.LocalDate;
import java.util.Set;

@Getter
@NoArgsConstructor
public class TripCreateRequestDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageUrl;
    private Set<Country> countries;

    @Builder
    private TripCreateRequestDto(String title, LocalDate startDate, LocalDate endDate, String imageUrl, Set<Country> countries) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.countries = countries;
    }
}

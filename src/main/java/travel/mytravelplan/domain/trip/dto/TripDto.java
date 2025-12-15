package travel.mytravelplan.domain.trip.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TripDto {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageUrl;

    @Builder
    private TripDto(Long id, String title, LocalDate startDate, LocalDate endDate, String imageUrl) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
    }
}

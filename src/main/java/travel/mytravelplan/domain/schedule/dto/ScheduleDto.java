package travel.mytravelplan.domain.schedule.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ScheduleDto {
    private Long id;
    private Long tripId;
    private String title;
    private String memo;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String placeName;
    private BigDecimal rating;

    @Builder
    private ScheduleDto(Long id, Long tripId, String title, String memo, LocalDateTime startDateTime, LocalDateTime endDateTime, String placeName, BigDecimal rating) {
        this.id = id;
        this.tripId = tripId;
        this.title = title;
        this.memo = memo;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.placeName = placeName;
        this.rating = rating;
    }
}

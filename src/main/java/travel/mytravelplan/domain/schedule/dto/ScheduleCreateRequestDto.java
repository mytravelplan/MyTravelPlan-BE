package travel.mytravelplan.domain.schedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleCreateRequestDto {
    private String title;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String memo;
    private BigDecimal rating;
    private Long placeId;

    @Builder
    private ScheduleCreateRequestDto(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, String memo, BigDecimal rating, Long placeId) {
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.memo = memo;
        this.rating = rating;
        this.placeId = placeId;
    }
}

package travel.mytravelplan.domain.place.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TripPlaceBookMarkDto {
    private Long tripPlaceId;
    private Long userId;
    private boolean bookmarked;

    @Builder
    private TripPlaceBookMarkDto(Long tripPlaceId, Long userId, boolean bookmarked) {
        this.tripPlaceId = tripPlaceId;
        this.userId = userId;
        this.bookmarked = bookmarked;
    }
}

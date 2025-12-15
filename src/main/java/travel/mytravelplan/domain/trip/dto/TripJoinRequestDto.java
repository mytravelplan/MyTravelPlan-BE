package travel.mytravelplan.domain.trip.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TripJoinRequestDto {
    private String inviteLink;

    @Builder
    private TripJoinRequestDto(String inviteLink) {
        this.inviteLink = inviteLink;
    }
}

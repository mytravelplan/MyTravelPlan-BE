package travel.mytravelplan.domain.trip.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TripInviteLinkDto {
    private String inviteLink;

    @Builder
    private TripInviteLinkDto(String inviteLink) {
        this.inviteLink = inviteLink;
    }
}

package travel.mytravelplan.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FollowDto {
    private Long id;
    private String username;
    private String nickname;
    private String profileImageUrl;
    private boolean following;

    @Builder
    private FollowDto(Long id, String username, String nickname, String profileImageUrl, boolean following) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.following = following;
    }
}

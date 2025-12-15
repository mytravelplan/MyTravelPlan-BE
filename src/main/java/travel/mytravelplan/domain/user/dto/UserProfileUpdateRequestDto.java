package travel.mytravelplan.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequestDto {
    private String nickname;
    private String profileImageUrl;
    private String introduction;
    private String websiteUrl;

    @Builder
    private UserProfileUpdateRequestDto(String nickname, String profileImageUrl, String introduction, String websiteUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.websiteUrl = websiteUrl;
    }
}

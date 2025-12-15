package travel.mytravelplan.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserProfileDto {
    private Long id;
    private String username;
    private String nickname;
    private String introduction;
    private String websiteUrl;
    private String profileImageUrl;
    private Long postCount;
    private Long followerCount;
    private Long followingCount;
    private boolean following;

    @Builder
    private UserProfileDto(Long id, String username, String nickname, String introduction, String websiteUrl, String profileImageUrl, Long postCount, Long followerCount, Long followingCount, boolean following) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.introduction = introduction;
        this.websiteUrl = websiteUrl;
        this.profileImageUrl = profileImageUrl;
        this.postCount = postCount;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.following = following;
    }
}

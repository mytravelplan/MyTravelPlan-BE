package travel.mytravelplan.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String profileImageUrl;

    private String introduction;

    private String websiteUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private UserProfile(String nickname, String profileImageUrl, String introduction, String websiteUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.websiteUrl = websiteUrl;
    }

    public static UserProfile createUserProfile(String nickname, String profileImageUrl) {
        return UserProfile.builder()
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
    }

    public void update(String nickname, String profileImageUrl, String introduction, String websiteUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.websiteUrl = websiteUrl;
    }
}

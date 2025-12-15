package travel.mytravelplan.global.security.oauth;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.security.oauth.userInfo.GoogleOauth2UserInfo;
import travel.mytravelplan.global.security.oauth.userInfo.KakaoOAuth2UserInfo;
import travel.mytravelplan.global.security.oauth.userInfo.NaverOAuth2UserInfo;
import travel.mytravelplan.global.security.oauth.userInfo.OAuth2UserInfo;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class OAuthAttributes {

    private OAuth2UserInfo oauth2UserInfo; // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 사진 등등)

    @Builder
    public OAuthAttributes(OAuth2UserInfo oauth2UserInfo) {
        this.oauth2UserInfo = oauth2UserInfo;
    }

    public static OAuthAttributes of(SocialType socialType,
                                    Map<String, Object> attributes) {

        if (socialType == SocialType.NAVER) {
            return ofNaver(attributes);
        }
        if (socialType == SocialType.KAKAO) {
            return ofKakao(attributes);
        }
        return ofGoogle(attributes);
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }

    public static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .oauth2UserInfo(new GoogleOauth2UserInfo(attributes))
                .build();
    }

    public static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .oauth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .build();
    }

    public User toEntity(SocialType socialType, OAuth2UserInfo oauth2UserInfo) {
        return User.createUser(
                socialType + "-" + oauth2UserInfo.providerId(),
                UUID.randomUUID().toString(),
                oauth2UserInfo.email(),
                socialType,
                oauth2UserInfo.providerId(),
                Set.of(Role.GUEST)
        );
    }
}

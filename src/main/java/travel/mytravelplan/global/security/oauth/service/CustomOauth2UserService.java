package travel.mytravelplan.global.security.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserProfileRepository;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.security.oauth.CustomOAuth2User;
import travel.mytravelplan.global.security.oauth.OAuthAttributes;
import travel.mytravelplan.global.security.oauth.userInfo.OAuth2UserInfo;

import java.util.Map;

import static travel.mytravelplan.domain.user.enums.SocialType.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Oauth 2.0 제공자 정보 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        SocialType socialType = getSocialType(registrationId);

        Map<String, Object> attributes = oAuth2User.getAttributes(); // 소셜 로그인 API에서 가져온 사용자 정보

        // SocialType에 따라서 사용자 정보를 추출 한다.
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(socialType, attributes);

         // SocialType과 attributes에 들어있는 소셜 로그인의 식별값 id를 통해 회원을 찾아 반환하는 메소드
         // 만약 찾은 회원이 있다면, 그대로 반환하고 없다면 saveUser()를 호출하여 회원을 저장한다.

        User user = getUser(oAuthAttributes, socialType);

        return new CustomOAuth2User(user, attributes);
    }

    private User getUser(OAuthAttributes attributes, SocialType socialType) {
        User findUser = userRepository.findBySocialTypeAndSocialId(socialType,
                attributes.getOauth2UserInfo().providerId()).orElse(null);

        if(findUser == null) {
            return saveUser(attributes, socialType);
        }
        return findUser;
    }

    private User saveUser(OAuthAttributes attributes, SocialType socialType) {
        User createdUser = attributes.toEntity(socialType, attributes.getOauth2UserInfo());

        OAuth2UserInfo oauth2UserInfo = attributes.getOauth2UserInfo();

        UserProfile userProfile = UserProfile.createUserProfile(oauth2UserInfo.name(), oauth2UserInfo.profileImageUrl());

        createdUser.setUserProfile(userProfile);

        userProfileRepository.save(userProfile);

        userRepository.save(createdUser);

        return createdUser;
    }


    private SocialType getSocialType(String registrationId) {
        return switch (registrationId) {
            case "google" -> GOOGLE;
            case "kakao" -> KAKAO;
            case "naver" -> NAVER;
            default -> LOCAL;
        };
    }
}

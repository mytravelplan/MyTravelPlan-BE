package travel.mytravelplan.global.security.oauth.userInfo;

import java.util.Map;

public class GoogleOauth2UserInfo extends OAuth2UserInfo{
    public GoogleOauth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String providerId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String email() {
        return (String) attributes.get("email");
    }

    @Override
    public String name() {
        return (String) attributes.get("name");
    }

    @Override
    public String profileImageUrl() {
        return (String) attributes.get("picture");
    }

}

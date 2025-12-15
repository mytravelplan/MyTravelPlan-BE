package travel.mytravelplan.global.security.oauth.userInfo;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String providerId();

    public abstract String email();

    public abstract String name();

    public abstract String profileImageUrl();

}

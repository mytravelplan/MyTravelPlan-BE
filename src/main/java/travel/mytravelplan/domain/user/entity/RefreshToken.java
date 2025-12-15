package travel.mytravelplan.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "refresh_token")
public class RefreshToken {
    @Id
    private Long userId;

    private String refreshToken;

    @TimeToLive
    private Long expirationPeriod;

    @Builder(access = AccessLevel.PRIVATE)
    private RefreshToken(Long userId, String refreshToken, Long expirationPeriod) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.expirationPeriod = expirationPeriod;
    }

    public static RefreshToken createRefreshToken(Long userId, String refreshToken, Long expirationPeriod) {
        return RefreshToken.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .expirationPeriod(expirationPeriod)
                .build();
    }

    public void update(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
    }
}

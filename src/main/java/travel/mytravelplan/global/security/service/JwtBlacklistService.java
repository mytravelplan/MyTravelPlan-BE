package travel.mytravelplan.global.security.service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JwtBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private final String BLACKLIST_PREFIX = "blacklist:";

    @Transactional
    public void addBlacklistedToken(String token, long expirationTime) {
        String blacklistedTokenKey = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(blacklistedTokenKey, "logout", expirationTime, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        String blacklistedTokenKey = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(blacklistedTokenKey);
    }
}

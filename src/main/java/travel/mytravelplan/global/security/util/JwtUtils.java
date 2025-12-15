package travel.mytravelplan.global.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.lang.Maps;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import travel.mytravelplan.domain.user.enums.Role;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpiration;

    private SecretKey accessSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(accessSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey refreshSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(refreshSecret);
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 엑세스 토큰 생성
     */
    public String createAccessToken(Long userId, Set<Role> roles) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("roles", roles)
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(accessSigningKey())
                .compact();
    }

    /**
     * 엑세스 토큰에서 사용자 ID 추출
     */
    public Long extractIdFromAccessToken(String accessToken) {
        String idString = Jwts.parser()
                .verifyWith(accessSigningKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getSubject();
        return Long.parseLong(idString);
    }

    /**
     * 엑세스 토큰에서 사용자 권한 추출
     */
    public Set<Role> extractIdFromAccessTokenRole(String token) {
        Set<String> roleStrings = Jwts.parser()
                .json(new JacksonDeserializer(Maps.of("roles", Set.class).build()))
                .verifyWith(accessSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles", Set.class);

        if (roleStrings == null) {
            return Set.of();
        }

        return roleStrings.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    /**
     * 엑세스 토근 유효성 검사
     */
    public boolean isAccessTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 리프래쉬 토큰 생성
     */
    public String createRefreshToken(Long userId, Set<Role> roles) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("roles", roles)
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(refreshSigningKey())
                .compact();
    }

    /**
     * 리프레시 토큰에서 사용자 ID 추출
     */
    public Long extractIdFromRefreshToken(String refreshToken) {
        String idString = Jwts.parser()
                .verifyWith(refreshSigningKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getSubject();

        return Long.parseLong(idString);
    }

    /**
     * 리프레시 토큰에서 사용자 권한 추출
     * (refresh 토큰에 roles 클레임이 없으면 빈 Set 반환)
     */
    public Set<Role> extractRoleFromRefreshToken(String refreshToken) {
        Set<String> roleStrings = Jwts.parser()
                .json(new JacksonDeserializer(Maps.of("roles", Set.class).build()))
                .verifyWith(refreshSigningKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .get("roles", Set.class);

        if (roleStrings == null) {
            return Set.of();
        }

        return roleStrings.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    /**
     * 리프래쉬 토큰 유효성 검사
     */
    public boolean isRefreshTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(refreshSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 엑세스 토큰 남은 유효기간 조회
    */
    public long getAccessTokenRemainingExpiration(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(accessSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 리프레시 토큰 남은 유효기간 조회
     */
    public long getRefreshTokenRemainingExpiration(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(refreshSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }
}

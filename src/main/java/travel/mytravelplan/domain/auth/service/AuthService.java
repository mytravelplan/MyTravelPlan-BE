package travel.mytravelplan.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.auth.dto.AdditionalInfoRequestDto;
import travel.mytravelplan.domain.auth.exception.AuthException;
import travel.mytravelplan.domain.auth.mapper.AuthMapper;
import travel.mytravelplan.domain.user.entity.RefreshToken;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.error.code.AuthErrorCode;
import travel.mytravelplan.global.security.repository.RefreshTokenRepository;
import travel.mytravelplan.global.security.service.JwtBlacklistService;
import travel.mytravelplan.global.security.util.CookieUtils;
import travel.mytravelplan.global.security.util.JwtUtils;

import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtils jwtUtils;
    private final JwtBlacklistService jwtBlacklistService;
    private final UserRepository userRepository;
    private final AuthMapper authMapper;
     private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = resolveAccessToken(request);

        long remainExpiration = jwtUtils.getAccessTokenRemainingExpiration(accessToken);

        jwtBlacklistService.addBlacklistedToken(accessToken, remainExpiration);

        String refreshToken = CookieUtils.getCookieValue(request, "RefreshToken");

        if(refreshToken == null) {
            throw new AccessDeniedException("쿠키에 리프래쉬 토큰이 존재하지 않습니다.");
        }

        if(!jwtUtils.isRefreshTokenValid(refreshToken)) {
            CookieUtils.deleteCookie(request, response, "RefreshToken");
            throw new AccessDeniedException("쿠키에 있는 리프래쉬 토큰이 유효하지 않습니다.");
        }

        // 여기서 부터 쿠키에 리프래쉬 토큰이 존재하고 유효한 경우
        Long userId = jwtUtils.extractIdFromRefreshToken(refreshToken);
        refreshTokenRepository.deleteByUserId(userId);

        CookieUtils.deleteCookie(request, response, "RefreshToken");
    }

    public void addAdditionalInfo(HttpServletRequest request, HttpServletResponse response, AdditionalInfoRequestDto additionalInfoRequestDto) {
        String refreshToken = CookieUtils.getCookieValue(request, "RefreshToken");

        if(refreshToken == null) {
            throw new AccessDeniedException("쿠키에 리프래쉬 토큰이 존재하지 않습니다.");
        }

        if(!jwtUtils.isRefreshTokenValid(refreshToken)) {
            CookieUtils.deleteCookie(request, response, "RefreshToken");
            throw new AccessDeniedException("쿠키에 있는 리프래쉬 토큰이 유효하지 않습니다.");
        }

        Long userId = jwtUtils.extractIdFromRefreshToken(refreshToken);
        Set<Role> roles = jwtUtils.extractRoleFromRefreshToken(refreshToken);

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(userId).orElse(null);

        if(refreshTokenEntity == null) {
            throw new AccessDeniedException("저장소에 리프래쉬 토큰이 존재하지 않습니다. 다시 로그인 해주세요.");
        }

        User user = userRepository.findById(refreshTokenEntity.getUserId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.NOT_FOUND_USER_IN_REFRESH_TOKEN));

        boolean isGuest = user.getRoles().stream().allMatch(role -> role == Role.GUEST);

        if (!isGuest) {
            throw new AuthException(AuthErrorCode.ALREADY_ADDED_ADDITIONAL_INFO);
        }

        user.updateAdditionalInfo(
                additionalInfoRequestDto.getBirthDate(),
                additionalInfoRequestDto.getPhoneNumber(),
                additionalInfoRequestDto.getGender(),
                additionalInfoRequestDto.getRoleType()
        );


        // 엑세스 토큰 재발급
        String accessToken = jwtUtils.createAccessToken(userId, roles);

        // 리프래쉬 토큰 재발급
        String newRefreshToken = jwtUtils.createRefreshToken(userId, roles);

        // RTR 정책에 따라 리프래쉬 토큰 갱신
        refreshTokenEntity.update(newRefreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        // 재발급 된 리프레쉬 토큰 응답 헤더와 쿠키에 담아주기
        response.setHeader("Authorization", "Bearer " + accessToken);
        CookieUtils.addCookie(response, "RefreshToken", newRefreshToken);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

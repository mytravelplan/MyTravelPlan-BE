package travel.mytravelplan.global.security.jwt.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.global.security.repository.RefreshTokenRepository;
import travel.mytravelplan.domain.user.entity.RefreshToken;
import travel.mytravelplan.global.security.jwt.CustomUserDetails;
import travel.mytravelplan.global.security.util.CookieUtils;
import travel.mytravelplan.global.security.util.JwtUtils;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 인증에 성공하면 Access Token을 생성하고, Refresh Token은 생성한 후 반환
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("JWT 인증 성공");
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Set<Role> roles = customUserDetails.getAuthorities().stream()
                .map(authority -> Role.valueOf(authority.getAuthority().replace("ROLE_", ""))).collect(Collectors.toSet());

        String accessToken = jwtUtils.createAccessToken(customUserDetails.getId(), roles);

        String refreshToken = jwtUtils.createRefreshToken(customUserDetails.getId(), roles);

        refreshTokenRepository.save(
                RefreshToken.createRefreshToken(customUserDetails.getId(), refreshToken, jwtUtils.getRefreshTokenRemainingExpiration(refreshToken))
        );

        // Access Token을 HEADER로 전달
        response.setHeader("Authorization", "Bearer " + accessToken);

        // 리프레쉬 토큰을 HttpOnly 쿠키로 전달
        CookieUtils.addCookie(response, "RefreshToken", refreshToken);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

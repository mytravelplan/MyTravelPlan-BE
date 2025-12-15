package travel.mytravelplan.global.security.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.global.security.repository.RefreshTokenRepository;
import travel.mytravelplan.domain.user.entity.RefreshToken;
import travel.mytravelplan.global.security.oauth.CustomOAuth2User;
import travel.mytravelplan.global.security.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import travel.mytravelplan.global.security.util.CookieUtils;
import travel.mytravelplan.global.security.util.JwtUtils;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Set<Role> roles = oAuth2User.getAuthorities().stream()
                .map(authority -> Role.valueOf(authority.getAuthority().replace("ROLE_", ""))).collect(Collectors.toSet());

        String refreshToken = jwtUtils.createRefreshToken(oAuth2User.getId(), roles);

        refreshTokenRepository.save(
                RefreshToken.createRefreshToken(oAuth2User.getId(), refreshToken, jwtUtils.getRefreshTokenRemainingExpiration(refreshToken))
        );

        // 추가 정보가 필요하면 프론트엔드의 추가 정보 입력 페이지로 리다이렉트
        if(oAuth2User.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_" + Role.GUEST.name()))) {
            response.sendRedirect("http://localhost:5173/auth/add-info");
            return;
        }

        // 리다이렉트 URL을 쿠키에서 가져옴 => 왜 이 상태에서 쿠키를 통해서 리다이렉션 URL 정보를 가져 올 수 있는지 아직 이해 안 감 OAuth Client 이해 필요
        String redirectUrl = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("http://localhost:5173");

        HttpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        response.sendRedirect(redirectUrl);
    }

    public Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
//        cookie.setSecure(true); // HTTPS에서만 전송
        cookie.setPath("/"); // 전체 경로에 대해 유효
        return cookie;
    }
}

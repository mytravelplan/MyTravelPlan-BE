package travel.mytravelplan.global.security.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.error.code.CommonErrorCode;
import travel.mytravelplan.global.security.repository.HttpCookieOAuth2AuthorizationRequestRepository;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");

        HttpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(CommonErrorCode.UNAUTHORIZED)));
    }
}

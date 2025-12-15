package travel.mytravelplan.global.security.jwt.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StreamUtils;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.exception.UserException;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.error.code.UserErrorCode;
import travel.mytravelplan.global.security.dto.LoginRequestDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class JwtLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final static String DEFAULT_FILTER_PROCESSES_URL = "/api/auth/login";

    private final ObjectMapper objectMapper;

    private final static String CONTENT_TYPE = "application/json";

    private final UserRepository userRepository;

    public JwtLoginAuthenticationFilter(ObjectMapper objectMapper, UserRepository userRepository) {
        super(DEFAULT_FILTER_PROCESSES_URL);
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if(request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)  ) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
        }

        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

        Map<String, String> usernamePasswordMap = objectMapper.readValue(messageBody, new TypeReference<>() {
        });

        String username = usernamePasswordMap.get("username");
        String password = usernamePasswordMap.get("password");

        userRepository.findByUsername(username).orElseThrow(() -> new AuthenticationServiceException("username does not exist: " + username));

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        return getAuthenticationManager().authenticate(authRequest);
    }
}

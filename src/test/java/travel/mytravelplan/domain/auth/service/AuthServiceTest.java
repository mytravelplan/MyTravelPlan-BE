package travel.mytravelplan.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import travel.mytravelplan.domain.auth.mapper.AuthMapper;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.security.service.JwtBlacklistService;
import travel.mytravelplan.global.security.util.JwtUtils;
import travel.mytravelplan.global.support.ServiceTestSupport;

@DisplayName("인증 서비스 테스트")
class AuthServiceTest extends ServiceTestSupport {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private JwtBlacklistService jwtBlacklistService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;
}
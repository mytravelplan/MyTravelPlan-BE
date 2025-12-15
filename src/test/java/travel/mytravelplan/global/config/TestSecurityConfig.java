package travel.mytravelplan.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.security.evaluator.MyTravelPlanPermissionEvaluator;
import travel.mytravelplan.global.security.jwt.filter.JwtAuthenticationFilter;
import travel.mytravelplan.global.security.jwt.filter.JwtLoginAuthenticationFilter;
import travel.mytravelplan.global.security.jwt.handler.JwtAccessDeniedHandler;
import travel.mytravelplan.global.security.jwt.handler.JwtAuthenticationEntryPoint;
import travel.mytravelplan.global.security.jwt.handler.JwtAuthenticationFailureHandler;
import travel.mytravelplan.global.security.jwt.handler.JwtAuthenticationSuccessHandler;

@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationSuccessHandler jwtAuthenticationSuccessHandler;
    private final JwtAuthenticationFailureHandler jwtAuthenticationFailureHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return http
                // CSRF 보호 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 미사용 설정 (JWT 사용)
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인증 실패 & 인가 실패 예외 처리
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .anyRequest().permitAll() // 모든 요청 허용
                )
                .addFilterBefore(jwtLoginAuthenticationFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, JwtLoginAuthenticationFilter.class)
                .build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
        ROLE_ADMIN > ROLE_SELLER
        ROLE_ADMIN > ROLE_USER
    """);
    }

    @Bean
    public JwtLoginAuthenticationFilter jwtLoginAuthenticationFilter(AuthenticationManager authenticationManager) {
        JwtLoginAuthenticationFilter filter = new JwtLoginAuthenticationFilter(objectMapper, userRepository);
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(jwtAuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(jwtAuthenticationFailureHandler);
        return filter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy, MyTravelPlanPermissionEvaluator evaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(evaluator);
        return handler;
    }
}

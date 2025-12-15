package travel.mytravelplan.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.security.evaluator.MyTravelPlanPermissionEvaluator;
import travel.mytravelplan.global.security.jwt.filter.JwtAuthenticationFilter;
import travel.mytravelplan.global.security.jwt.filter.JwtLoginAuthenticationFilter;
import travel.mytravelplan.global.security.jwt.handler.*;
import travel.mytravelplan.global.security.oauth.handler.OAuth2LoginFailureHandler;
import travel.mytravelplan.global.security.oauth.handler.OAuth2LoginSuccessHandler;
import travel.mytravelplan.global.security.oauth.service.CustomOauth2UserService;
import travel.mytravelplan.global.security.repository.HttpCookieOAuth2AuthorizationRequestRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationSuccessHandler jwtAuthenticationSuccessHandler;
    private final JwtAuthenticationFailureHandler jwtAuthenticationFailureHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomOauth2UserService customOauth2UserService;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        http
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

            .// CORS 설정
            cors(cors -> cors.configurationSource(corsConfigurationSource()))
/*
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                    .requestMatchers(HttpMethod.POST, "/auth/login", "/api/users").permitAll()
                    .requestMatchers(HttpMethod.GET,"/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                    .anyRequest().authenticated()
            )
*/
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                    .anyRequest().permitAll() // 모든 요청 허용
            )
            .addFilterBefore(jwtLoginAuthenticationFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, JwtLoginAuthenticationFilter.class)
            .oauth2Login((oauth2) -> oauth2
                    .userInfoEndpoint(
                            (userInfoEndpointConfig -> userInfoEndpointConfig
                                    .userService(customOauth2UserService)
                            )
                    )
                    .authorizationEndpoint(authorizationEndpointConfig ->
                            authorizationEndpointConfig
                                    .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository())
                    )
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler)
            );

        return http.build();
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
    public HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 권환 계층 구조
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
        ROLE_ADMIN > ROLE_SELLER
        ROLE_ADMIN > ROLE_USER
    """);
    }
    // 커스텀 PermissionEvaluator 설정
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy, MyTravelPlanPermissionEvaluator evaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(evaluator);
        return handler;
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처 설정
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 허용할 HTTP 메소드 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "PATCH"));

        // 허용할 헤더 설정
        configuration.setAllowedHeaders(List.of("*"));

        // 자격 증명 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // 브라우저에서 접근할 수 있는 응답 헤더
        configuration.setExposedHeaders(List.of("Authorization"));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

package travel.mytravelplan.global.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.security.jwt.CustomUserDetails;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = User.createUser(
                        annotation.username(),
                        annotation.password(),
                        "test@example.com",
                        SocialType.LOCAL,
                        null,
                        Arrays.stream(annotation.roles()).map(role -> Enum.valueOf(Role.class, role)).collect(Collectors.toSet()));

        ReflectionTestUtils.setField(user, "id", annotation.id());

        CustomUserDetails principal = new CustomUserDetails(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());

        context.setAuthentication(auth);
        return context;
    }
}

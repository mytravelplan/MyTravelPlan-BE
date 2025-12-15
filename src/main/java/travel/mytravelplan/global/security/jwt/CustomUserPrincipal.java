package travel.mytravelplan.global.security.jwt;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.user.enums.Role;

import java.util.Set;

@Getter
public class CustomUserPrincipal {
    private Long id;
    private Set<Role> roles;

    @Builder
    private CustomUserPrincipal(Long id, Set<Role> roles) {
        this.id = id;
        this.roles = roles;
    }
}

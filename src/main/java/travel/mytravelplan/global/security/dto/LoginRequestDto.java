package travel.mytravelplan.global.security.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {
    private String loginId;
    private String password;

    @Builder
    private LoginRequestDto(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }
}

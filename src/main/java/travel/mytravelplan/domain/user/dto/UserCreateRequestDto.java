package travel.mytravelplan.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;

import java.time.LocalDate;
import java.util.Set;

@Getter
@NoArgsConstructor
public class UserCreateRequestDto {
    private String username;
    private String password;
    private String email;
    private LocalDate birth;
    private String phoneNumber;
    private Gender gender;
    private String nickname;
    private String profileImageUrl;
    private Set<Role> roles;

    @Builder
    private UserCreateRequestDto(String username, String password, String email, LocalDate birth, String phoneNumber, Gender gender, String nickname, String profileImageUrl, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.roles = roles;
    }
}

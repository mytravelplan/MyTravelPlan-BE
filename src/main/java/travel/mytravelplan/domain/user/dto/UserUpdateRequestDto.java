package travel.mytravelplan.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.user.enums.Gender;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserUpdateRequestDto {
    private String username;
    private String password;
    private String email;
    private LocalDate birth;
    private String phoneNumber;
    private Gender gender;

    @Builder
    private UserUpdateRequestDto(String username, String password, String email, LocalDate birth, String phoneNumber, Gender gender) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }
}

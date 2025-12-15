package travel.mytravelplan.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.SocialType;

import java.time.LocalDate;

@Getter
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private LocalDate birth;
    private String phoneNumber;
    private Gender gender;
    private SocialType socialType;
    private String socialId;

    @Builder
    private UserDto(Long id, String username, String email, LocalDate birth, String phoneNumber, Gender gender, SocialType socialType, String socialId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.socialType = socialType;
        this.socialId = socialId;
    }
}

package travel.mytravelplan.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AdditionalInfoRequestDto {
    private LocalDate birthDate;
    private String phoneNumber;
    private Gender gender;
    private Role roleType;

    @Builder
    private AdditionalInfoRequestDto(LocalDate birthDate, String phoneNumber, Gender gender, Role roleType) {
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.roleType = roleType;
    }
}

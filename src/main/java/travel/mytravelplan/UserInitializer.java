package travel.mytravelplan;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserProfileRepository;
import travel.mytravelplan.domain.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.Set;

@Profile("local")
@Component
@Order(1)
@RequiredArgsConstructor
public class UserInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        User user1 = User.createUser(
                "cksgud0403",
                passwordEncoder.encode("testpassword"),
                "cksgud0403@naver.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(2002, 4, 3),
                "010-0000-0000",
                Gender.MALE,
                Set.of(Role.USER)
        );

        UserProfile userProfile1 = UserProfile.createUserProfile("cksgud0403", "https://mytravelplan-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png");
        user1.setUserProfile(userProfile1);

        User user2 = User.createUser(
                "cksguddl0403",
                passwordEncoder.encode("testpassword"),
                "cksguddl0403@naver.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(2002, 4, 3),
                "010-0000-0000",
                Gender.MALE,
                Set.of(Role.USER)
        );

        UserProfile userProfile2 = UserProfile.createUserProfile("cksguddl0403", "https://mytravelplan-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png");
        user2.setUserProfile(userProfile2);

        User user3 = User.createUser(
                "chanhyeong0403",
                passwordEncoder.encode("testpassword"),
                "chanhyeong0403@naver.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(2002, 4, 3),
                "010-0000-0000",
                Gender.MALE,
                Set.of(Role.USER)
        );

        UserProfile userProfile3 = UserProfile.createUserProfile("chanhyeong0403", "https://mytravelplan-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png");
        user3.setUserProfile(userProfile3);

        userProfileRepository.save(userProfile1);
        userProfileRepository.save(userProfile2);
        userProfileRepository.save(userProfile3);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
    }
}

package travel.mytravelplan.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.cart.entity.Cart;
import travel.mytravelplan.domain.delivery.entity.DeliveryAddress;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 및 인증 관련
    private String username;

    private String password;

    private String email;

    // 소셜 로그인
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;

    // 개인 정보
    private LocalDate birth;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user")
    private List<DeliveryAddress> deliveryAddresses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Cart> carts = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private User(String username, String password, String email, SocialType socialType, String socialId, LocalDate birth, String phoneNumber, Gender gender, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.socialType = socialType;
        this.socialId = socialId;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.roles = roles;
    }

    public static User createUser(String username, String password, String email, SocialType socialType, String socialId, LocalDate birth, String phoneNumber, Gender gender, Set<Role> roles) {
        return User.builder()
                .username(username)
                .password(password)
                .email(email)
                .socialType(socialType)
                .socialId(socialId)
                .birth(birth)
                .phoneNumber(phoneNumber)
                .gender(gender)
                .roles(roles)
                .build();
    }

    public static User createUser(String username, String password, String email, SocialType socialType, String socialId, Set<Role> roles) {
        return User.builder()
                .username(username)
                .password(password)
                .email(email)
                .socialType(socialType)
                .socialId(socialId)
                .roles(roles)
                .build();
    }

    public void addCart(Cart cart) {
        this.carts.add(cart);
        cart.setUser(this);
    }

    public void updateInfo(String username, String password, String email, LocalDate birth, String phoneNumber, Gender gender) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    public void updateProfile(String nickname, String profileImageUrl, String introduction, String websiteUrl) {
        userProfile.update(nickname, profileImageUrl, introduction, websiteUrl);
    }

    public void updateAdditionalInfo(LocalDate birthDate, String phoneNumber, Gender gender, Role roleType) {
        this.birth = birthDate;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.roles.clear();
        this.roles.add(roleType);
    }
}

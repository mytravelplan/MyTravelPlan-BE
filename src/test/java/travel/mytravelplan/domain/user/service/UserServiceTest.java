package travel.mytravelplan.domain.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.user.dto.*;
import travel.mytravelplan.domain.user.entity.Follow;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.exception.UserException;
import travel.mytravelplan.domain.user.mapper.FollowMapper;
import travel.mytravelplan.domain.user.mapper.UserMapper;
import travel.mytravelplan.domain.user.mapper.UserProfileMapper;
import travel.mytravelplan.domain.user.repository.FollowRepository;
import travel.mytravelplan.domain.user.repository.UserProfileRepository;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.UserErrorCode;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("사용자 서비스 테스트")
class UserServiceTest extends ServiceTestSupport {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private FollowMapper followMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserProfile userProfile;
    private UserCreateRequestDto userCreateRequestDto;
    private UserUpdateRequestDto userUpdateRequestDto;
    private UserProfileUpdateRequestDto userProfileUpdateRequestDto;

    @BeforeEach
    void setUp() {
        userProfile = UserProfile.createUserProfile("테스트닉네임", "profile.jpg");

        user = User.createUser(
                "testuser",
                "encodedPassword",
                "test@example.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        // ReflectionTestUtils를 사용하여 User 엔티티의 id 필드에 값 설정
        ReflectionTestUtils.setField(user, "id", 1L);

        user.setUserProfile(userProfile);

        userCreateRequestDto = UserCreateRequestDto.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .nickname("테스트닉네임")
                .profileImageUrl("profile.jpg")
                .birth(LocalDate.of(1990, 1, 1))
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .roles(Set.of(Role.USER))
                .build();

        userUpdateRequestDto = UserUpdateRequestDto.builder()
                .username("updateduser")
                .password("newpassword123")
                .email("updated@example.com")
                .birth(LocalDate.of(1990, 1, 1))
                .phoneNumber("010-9876-5432")
                .gender(Gender.FEMALE)
                .build();

        userProfileUpdateRequestDto = UserProfileUpdateRequestDto.builder()
                .nickname("업데이트닉네임")
                .profileImageUrl("updated-profile.jpg")
                .introduction("안녕하세요")
                .websiteUrl("https://example.com")
                .build();
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void join_Success() {
        // given
        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.empty());
        given(passwordEncoder.encode(eq("password123"))).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);
        given(userProfileRepository.save(any(UserProfile.class))).willReturn(userProfile);

        UserDto expectedDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

        // when
        UserDto result = userService.join(userCreateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        then(userRepository).should().findByUsername(eq("testuser"));
        then(passwordEncoder).should().encode(eq("password123"));
        then(userProfileRepository).should().save(any(UserProfile.class));
        then(userRepository).should().save(any(User.class));
        then(userMapper).should().toDto(any(User.class));
    }

    @Test
    @DisplayName("회원가입 - 중복된 사용자명으로 실패")
    void join_DuplicateUsername_ThrowsException() {
        // given
        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.join(userCreateRequestDto))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.DUPLICATE_USER);

        then(userRepository).should().findByUsername(eq("testuser"));
    }

    @Test
    @DisplayName("사용자 정보 조회 - 성공")
    void getUserInfo_Success() {
        // given
        Long userId = 1L;
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();
        given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

        // when
        UserDto result = userService.getUserInfo(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");

        then(userRepository).should().findById(eq(userId));
        then(userMapper).should().toDto(any(User.class));
    }

    @Test
    @DisplayName("사용자 정보 조회 - 존재하지 않는 사용자")
    void getUserInfo_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserInfo(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findById(eq(userId));
    }

    @Test
    @DisplayName("사용자 정보 수정 - 성공")
    void updateUserInfo_Success() {
        // given
        Long userId = 1L;
        given(userRepository.findByUsername(eq("updateduser"))).willReturn(Optional.empty());
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(passwordEncoder.encode(eq("newpassword123"))).willReturn("newEncodedPassword");

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .username("updateduser")
                .email("updated@example.com")
                .build();
        given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

        // when
        UserDto result = userService.updateUserInfo(userId, userUpdateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");

        then(userRepository).should().findByUsername(eq("updateduser"));
        then(userRepository).should().findById(eq(userId));
        then(passwordEncoder).should().encode(eq("newpassword123"));
        then(userMapper).should().toDto(any(User.class));
    }

    @Test
    @DisplayName("사용자 정보 수정 - 존재하지 않는 사용자")
    void updateUserInfo_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        given(userRepository.findByUsername(eq("updateduser"))).willReturn(Optional.empty());
        given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserInfo(userId, userUpdateRequestDto))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findByUsername(eq("updateduser"));
        then(userRepository).should().findById(eq(userId));
    }

    @Test
    @DisplayName("사용자 정보 수정 - 중복된 사용자명으로 실패")
    void updateUserInfo_DuplicateUsername_ThrowsException() {
        // given
        Long userId = 1L;
        User existingUser = User.createUser(
                "updateduser",
                "password",
                "existing@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        given(userRepository.findByUsername(eq("updateduser"))).willReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.updateUserInfo(userId, userUpdateRequestDto))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.DUPLICATE_USER);

        then(userRepository).should().findByUsername(eq("updateduser"));
    }

    @Test
    @DisplayName("사용자 프로필 조회 - 성공")
    void getUserProfile_Success() {
        // given
        Long userId = 1L;
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(false);

        UserProfileDto expectedDto = UserProfileDto.builder()
                .id(userId)
                .username("testuser")
                .nickname("테스트닉네임")
                .following(false)
                .build();
        given(userProfileMapper.toDto(any(User.class), eq(false))).willReturn(expectedDto);

        // when
        UserProfileDto result = userService.getUserProfile(currentUser, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getNickname()).isEqualTo("테스트닉네임");
        assertThat(result.isFollowing()).isFalse();

        then(userRepository).should().findById(eq(userId));
        then(followRepository).should().existsByFollowerAndFollowing(any(User.class), any(User.class));
        then(userProfileMapper).should().toDto(any(User.class), eq(false));
    }

    @Test
    @DisplayName("사용자 프로필 조회 - 존재하지 않는 사용자")
    void getUserProfile_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserProfile(currentUser, userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findById(eq(userId));
    }

    @Test
    @DisplayName("사용자 프로필 수정 - 성공")
    void updateUserProfile_Success() {
        // given
        Long userId = 1L;
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(false);

        UserProfileDto expectedDto = UserProfileDto.builder()
                .id(userId)
                .username("testuser")
                .nickname("업데이트닉네임")
                .following(false)
                .build();
        given(userProfileMapper.toDto(any(User.class), eq(false))).willReturn(expectedDto);

        // when
        UserProfileDto result = userService.updateUserProfile(currentUser, userId, userProfileUpdateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("업데이트닉네임");

        then(userRepository).should().findById(eq(userId));
        then(followRepository).should().existsByFollowerAndFollowing(any(User.class), any(User.class));
        then(userProfileMapper).should().toDto(any(User.class), eq(false));
    }

    @Test
    @DisplayName("사용자 프로필 수정 - 존재하지 않는 사용자")
    void updateUserProfile_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile(currentUser, userId, userProfileUpdateRequestDto))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findById(eq(userId));
    }

    @Test
    @DisplayName("팔로우 - 성공")
    void followUser_Success() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(false);

        // when
        userService.followUser(currentUser, "testuser");

        // then
        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().existsByFollowerAndFollowing(any(User.class), any(User.class));
        then(followRepository).should().save(any(Follow.class));
    }

    @Test
    @DisplayName("팔로우 - 존재하지 않는 사용자")
    void followUser_UserNotFound_ThrowsException() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("nonexistentuser"))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.followUser(currentUser, "nonexistentuser"))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findByUsername(eq("nonexistentuser"));
    }

    @Test
    @DisplayName("팔로우 - 자기 자신을 팔로우할 수 없음")
    void followUser_CannotFollowSelf_ThrowsException() {
        // given
        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.followUser(user, "testuser"))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_CANNOT_FOLLOW_SELF);

        then(userRepository).should().findByUsername(eq("testuser"));
    }

    @Test
    @DisplayName("팔로우 - 이미 팔로우한 사용자")
    void followUser_AlreadyFollowing_ThrowsException() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.followUser(currentUser, "testuser"))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_ALREADY_FOLLOWING);

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().existsByFollowerAndFollowing(any(User.class), any(User.class));
    }

    @Test
    @DisplayName("언팔로우 - 성공")
    void unfollowUser_Success() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));

        // when
        userService.unfollowUser(currentUser, "testuser");

        // then
        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().deleteByFollowerAndFollowing(any(User.class), any(User.class));
    }

    @Test
    @DisplayName("언팔로우 - 존재하지 않는 사용자")
    void unfollowUser_UserNotFound_ThrowsException() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("nonexistentuser"))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.unfollowUser(currentUser, "nonexistentuser"))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findByUsername(eq("nonexistentuser"));
    }

    @Test
    @DisplayName("언팔로우 - 자기 자신을 언팔로우할 수 없음")
    void unfollowUser_CannotUnfollowSelf_ThrowsException() {
        // given
        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.unfollowUser(user, "testuser"))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_CANNOT_FOLLOW_SELF);

        then(userRepository).should().findByUsername(eq("testuser"));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 성공")
    void getUserFollowers_Success() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User follower1 = User.createUser(
                "follower1",
                "password",
                "follower1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(follower1, "id", 3L);

        Follow follow = Follow.createFollow(follower1, user);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of(follow));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(false);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("follower1")
                .following(false)
                .build();
        given(followMapper.toDto(any(User.class), eq(false))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowers(
                currentUser, "testuser", "", "createdAt", "DESC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 존재하지 않는 사용자")
    void getUserFollowers_UserNotFound() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("nonexistentuser"))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserFollowers(
                currentUser, "nonexistentuser", "", "createdAt", "DESC", "", null, 10
        ))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findByUsername(eq("nonexistentuser"));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 페이지네이션 hasNext true")
    void getUserFollowers_WithPagination() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User follower1 = User.createUser(
                "follower1",
                "password",
                "follower1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(follower1, "id", 3L);

        User follower2 = User.createUser(
                "follower2",
                "password",
                "follower2@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(follower2, "id", 4L);

        User follower3 = User.createUser(
                "follower3",
                "password",
                "follower3@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(follower3, "id", 5L);

        Follow follow1 = Follow.createFollow(follower1, user);
        ReflectionTestUtils.setField(follow1, "id", 1L);
        ReflectionTestUtils.setField(follow1, "createdAt", LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        Follow follow2 = Follow.createFollow(follower2, user);
        ReflectionTestUtils.setField(follow2, "id", 2L);
        ReflectionTestUtils.setField(follow2, "createdAt", LocalDateTime.of(2024, 12, 2, 10, 0, 0));
        Follow follow3 = Follow.createFollow(follower3, user);
        ReflectionTestUtils.setField(follow3, "id", 3L);
        ReflectionTestUtils.setField(follow3, "createdAt", LocalDateTime.of(2024, 12, 3, 10, 0, 0));

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(3)))
                .willReturn(List.of(follow1, follow2, follow3));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(false);

        FollowDto followDto1 = FollowDto.builder()
                .id(3L)
                .username("follower1")
                .following(false)
                .build();
        FollowDto followDto2 = FollowDto.builder()
                .id(4L)
                .username("follower2")
                .following(false)
                .build();
        given(followMapper.toDto(eq(follower1), eq(false))).willReturn(followDto1);
        given(followMapper.toDto(eq(follower2), eq(false))).willReturn(followDto2);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowers(
                currentUser, "testuser", "", "createdAt", "DESC", "", null, 2
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(3));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 빈 목록")
    void getUserFollowers_EmptyList() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowers(
                currentUser, "testuser", "", "createdAt", "DESC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(0);

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 성공")
    void getUserFollowing_Success() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User following1 = User.createUser(
                "following1",
                "password",
                "following1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(following1, "id", 3L);

        Follow follow = Follow.createFollow(user, following1);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of(follow));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(true);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("following1")
                .following(true)
                .build();
        given(followMapper.toDto(any(User.class), eq(true))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowing(
                currentUser, "testuser", "", "createdAt", "DESC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 존재하지 않는 사용자")
    void getUserFollowing_UserNotFound() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("nonexistentuser"))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserFollowing(
                currentUser, "nonexistentuser", "", "createdAt", "DESC", "", null, 10
        ))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findByUsername(eq("nonexistentuser"));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 페이지네이션 hasNext true")
    void getUserFollowing_WithPagination() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User following1 = User.createUser(
                "following1",
                "password",
                "following1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(following1, "id", 3L);

        User following2 = User.createUser(
                "following2",
                "password",
                "following2@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(following2, "id", 4L);

        User following3 = User.createUser(
                "following3",
                "password",
                "following3@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(following3, "id", 5L);

        Follow follow1 = Follow.createFollow(user, following1);
        ReflectionTestUtils.setField(follow1, "id", 1L);
        ReflectionTestUtils.setField(follow1, "createdAt", LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        Follow follow2 = Follow.createFollow(user, following2);
        ReflectionTestUtils.setField(follow2, "id", 2L);
        ReflectionTestUtils.setField(follow2, "createdAt", LocalDateTime.of(2024, 12, 2, 10, 0, 0));
        Follow follow3 = Follow.createFollow(user, following3);
        ReflectionTestUtils.setField(follow3, "id", 3L);
        ReflectionTestUtils.setField(follow3, "createdAt", LocalDateTime.of(2024, 12, 3, 10, 0, 0));

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(3)))
                .willReturn(List.of(follow1, follow2, follow3));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(true);

        FollowDto followDto1 = FollowDto.builder()
                .id(3L)
                .username("following1")
                .following(true)
                .build();
        FollowDto followDto2 = FollowDto.builder()
                .id(4L)
                .username("following2")
                .following(true)
                .build();
        given(followMapper.toDto(eq(following1), eq(true))).willReturn(followDto1);
        given(followMapper.toDto(eq(following2), eq(true))).willReturn(followDto2);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowing(
                currentUser, "testuser", "", "createdAt", "DESC", "", null, 2
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextAfter()).isEqualTo(2L);

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(3));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 빈 목록")
    void getUserFollowing_EmptyList() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of());

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowing(
                currentUser, "testuser", "", "createdAt", "DESC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(0);

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 키워드 검색")
    void getUserFollowers_WithKeyword() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User follower1 = User.createUser(
                "searchuser",
                "password",
                "search@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(follower1, "id", 3L);

        Follow follow = Follow.createFollow(follower1, user);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowers(eq(1L), eq("search"), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of(follow));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(false);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("searchuser")
                .following(false)
                .build();
        given(followMapper.toDto(any(User.class), eq(false))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowers(
                currentUser, "testuser", "search", "createdAt", "DESC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowers(eq(1L), eq("search"), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 커서 기반 페이지네이션")
    void getUserFollowers_WithCursor() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User follower1 = User.createUser(
                "follower1",
                "password",
                "follower1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(follower1, "id", 3L);

        Follow follow = Follow.createFollow(follower1, user);
        ReflectionTestUtils.setField(follow, "id", 5L);

        String cursor = "2024-12-04T10:00:00";
        Long after = 3L;

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(cursor), eq(after), eq(11)))
                .willReturn(List.of(follow));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(false);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("follower1")
                .following(false)
                .build();
        given(followMapper.toDto(any(User.class), eq(false))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowers(
                currentUser, "testuser", "", "createdAt", "DESC", cursor, after, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(cursor), eq(after), eq(11));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - ASC 정렬")
    void getUserFollowers_WithAscOrder() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User follower1 = User.createUser(
                "follower1",
                "password",
                "follower1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(follower1, "id", 3L);

        Follow follow = Follow.createFollow(follower1, user);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("ASC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of(follow));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(false);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("follower1")
                .following(false)
                .build();
        given(followMapper.toDto(any(User.class), eq(false))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowers(
                currentUser, "testuser", "", "createdAt", "ASC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("ASC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 키워드 검색")
    void getUserFollowing_WithKeyword() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User following1 = User.createUser(
                "searchuser",
                "password",
                "search@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(following1, "id", 3L);

        Follow follow = Follow.createFollow(user, following1);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowings(eq(1L), eq("search"), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of(follow));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(true);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("searchuser")
                .following(true)
                .build();
        given(followMapper.toDto(any(User.class), eq(true))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowing(
                currentUser, "testuser", "search", "createdAt", "DESC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowings(eq(1L), eq("search"), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 커서 기반 페이지네이션")
    void getUserFollowing_WithCursor() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User following1 = User.createUser(
                "following1",
                "password",
                "following1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(following1, "id", 3L);

        Follow follow = Follow.createFollow(user, following1);
        ReflectionTestUtils.setField(follow, "id", 5L);

        String cursor = "2024-12-04T10:00:00";
        Long after = 3L;

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(cursor), eq(after), eq(11)))
                .willReturn(List.of(follow));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(true);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("following1")
                .following(true)
                .build();
        given(followMapper.toDto(any(User.class), eq(true))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowing(
                currentUser, "testuser", "", "createdAt", "DESC", cursor, after, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(cursor), eq(after), eq(11));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - ASC 정렬")
    void getUserFollowing_WithAscOrder() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User following1 = User.createUser(
                "following1",
                "password",
                "following1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(following1, "id", 3L);

        Follow follow = Follow.createFollow(user, following1);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("ASC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of(follow));
        given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(true);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("following1")
                .following(true)
                .build();
        given(followMapper.toDto(any(User.class), eq(true))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowing(
                currentUser, "testuser", "", "createdAt", "ASC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("ASC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 현재 사용자가 팔로워를 팔로우하고 있는 경우")
    void getUserFollowers_CurrentUserFollowingFollower() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User follower1 = User.createUser(
                "follower1",
                "password",
                "follower1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(follower1, "id", 3L);

        Follow follow = Follow.createFollow(follower1, user);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of(follow));
        // currentUser가 follower1을 팔로우하고 있으므로 true 반환
        given(followRepository.existsByFollowerAndFollowing(eq(follower1), eq(currentUser))).willReturn(true);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("follower1")
                .following(true)
                .build();
        given(followMapper.toDto(eq(follower1), eq(true))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowers(
                currentUser, "testuser", "", "createdAt", "DESC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().isFollowing()).isTrue();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowers(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 현재 사용자가 팔로잉을 팔로우하지 않는 경우")
    void getUserFollowing_CurrentUserNotFollowingFollowing() {
        // given
        User currentUser = User.createUser(
                "currentuser",
                "password",
                "current@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(currentUser, "id", 2L);

        User following1 = User.createUser(
                "following1",
                "password",
                "following1@example.com",
                SocialType.LOCAL,
                null,
                Set.of(Role.USER)
        );
        ReflectionTestUtils.setField(following1, "id", 3L);

        Follow follow = Follow.createFollow(user, following1);

        given(userRepository.findByUsername(eq("testuser"))).willReturn(Optional.of(user));
        given(followRepository.findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11)))
                .willReturn(List.of(follow));
        // currentUser가 following1을 팔로우하지 않으므로 false 반환
        given(followRepository.existsByFollowerAndFollowing(eq(currentUser), eq(following1))).willReturn(false);

        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("following1")
                .following(false)
                .build();
        given(followMapper.toDto(eq(following1), eq(false))).willReturn(followDto);

        // when
        CursorPageResponseDto<FollowDto> result = userService.getUserFollowing(
                currentUser, "testuser", "", "createdAt", "DESC", "", null, 10
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().isFollowing()).isFalse();

        then(userRepository).should().findByUsername(eq("testuser"));
        then(followRepository).should().findAllFollowings(eq(1L), eq(""), eq("createdAt"), eq("DESC"), eq(""), eq(null), eq(11));
    }
}
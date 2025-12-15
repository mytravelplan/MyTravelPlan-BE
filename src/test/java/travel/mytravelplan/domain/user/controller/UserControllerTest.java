package travel.mytravelplan.domain.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.post.dto.PostDto;
import travel.mytravelplan.domain.post.service.PostService;
import travel.mytravelplan.domain.user.dto.*;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.service.UserService;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ControllerTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class)
@DisplayName("사용자 컨트롤러 테스트")
public class UserControllerTest extends ControllerTestSupport {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PostService postService;

    private String accessToken;
    private Long userId;
    private User testUser;
    private UserDto userDto;
    private UserProfileDto userProfileDto;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistService.isTokenBlacklisted(any(String.class))).willReturn(false);

        UserProfile userProfile = UserProfile.createUserProfile(
                "테스트유저",
                "http://example.com/profile.jpg"
        );
        ReflectionTestUtils.setField(userProfile, "introduction", "안녕하세요");
        ReflectionTestUtils.setField(userProfile, "websiteUrl", "http://example.com");

        testUser = User.createUser(
                "testUser",
                "password123",
                "test@test.com",
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );

        testUser.setUserProfile(userProfile);

        userId = 1L;
        ReflectionTestUtils.setField(testUser, "id", userId);

        accessToken = jwtUtils.createAccessToken(userId, Set.of(Role.USER));

        given(userRepository.findById(eq(userId))).willReturn(Optional.of(testUser));

        userDto = UserDto.builder()
                .id(userId)
                .username("testUser")
                .email("test@test.com")
                .birth(LocalDate.of(1990, 1, 1))
                .phoneNumber("010-1234-5678")
                .gender(Gender.MALE)
                .socialType(SocialType.LOCAL)
                .socialId(null)
                .build();

        userProfileDto = UserProfileDto.builder()
                .id(userId)
                .username("testUser")
                .nickname("테스트유저")
                .introduction("안녕하세요")
                .websiteUrl("http://example.com")
                .profileImageUrl("http://example.com/profile.jpg")
                .postCount(10L)
                .followerCount(5L)
                .followingCount(3L)
                .following(false)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void createUser_Success() throws Exception {
        // given
        UserCreateRequestDto createRequestDto = UserCreateRequestDto.builder()
                .username("newUser")
                .password("password123")
                .email("new@test.com")
                .birth(LocalDate.of(1995, 5, 15))
                .phoneNumber("010-9876-5432")
                .gender(Gender.FEMALE)
                .nickname("신규유저")
                .profileImageUrl("http://example.com/new.jpg")
                .roles(Set.of(Role.USER))
                .build();

        UserDto newUserDto = UserDto.builder()
                .id(2L)
                .username("newUser")
                .email("new@test.com")
                .birth(LocalDate.of(1995, 5, 15))
                .phoneNumber("010-9876-5432")
                .gender(Gender.FEMALE)
                .socialType(SocialType.LOCAL)
                .build();

        given(userService.join(any(UserCreateRequestDto.class))).willReturn(newUserDto);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("newUser"))
                .andExpect(jsonPath("$.data.email").value("new@test.com"))
                .andDo(document("user-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").description("사용자명"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("birth").description("생년월일"),
                                fieldWithPath("phoneNumber").description("전화번호"),
                                fieldWithPath("gender").description("성별 (MALE, FEMALE)"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("profileImageUrl").description("프로필 이미지 URL"),
                                fieldWithPath("roles").description("권한 목록")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("사용자 ID"),
                                fieldWithPath("data.username").description("사용자명"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.birth").description("생년월일"),
                                fieldWithPath("data.phoneNumber").description("전화번호"),
                                fieldWithPath("data.gender").description("성별"),
                                fieldWithPath("data.socialType").description("소셜 타입"),
                                fieldWithPath("data.socialId").description("소셜 ID").optional()
                        )
                ));

        assertThat(newUserDto).isNotNull();
        assertThat(newUserDto.getUsername()).isEqualTo("newUser");
        then(userService).should().join(any(UserCreateRequestDto.class));
    }

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() throws Exception {
        // given
        given(userService.getUserInfo(eq(userId))).willReturn(userDto);

        // when & then
        mockMvc.perform(get("/api/users/{userId}", userId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andDo(document("user-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("userId").description("사용자 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("사용자 ID"),
                                fieldWithPath("data.username").description("사용자명"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.birth").description("생년월일"),
                                fieldWithPath("data.phoneNumber").description("전화번호"),
                                fieldWithPath("data.gender").description("성별"),
                                fieldWithPath("data.socialType").description("소셜 타입"),
                                fieldWithPath("data.socialId").description("소셜 ID").optional()
                        )
                ));

        assertThat(userDto).isNotNull();
        assertThat(userDto.getId()).isEqualTo(userId);
        then(userService).should().getUserInfo(eq(userId));
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUserInfo_Success() throws Exception {
        // given
        UserUpdateRequestDto updateRequestDto = UserUpdateRequestDto.builder()
                .username("updatedUser")
                .password("newPassword123")
                .email("updated@test.com")
                .birth(LocalDate.of(1990, 1, 1))
                .phoneNumber("010-1111-2222")
                .gender(Gender.MALE)
                .build();

        UserDto updatedUserDto = UserDto.builder()
                .id(userId)
                .username("updatedUser")
                .email("updated@test.com")
                .birth(LocalDate.of(1990, 1, 1))
                .phoneNumber("010-1111-2222")
                .gender(Gender.MALE)
                .socialType(SocialType.LOCAL)
                .build();

        given(userService.updateUserInfo(eq(userId), any(UserUpdateRequestDto.class)))
                .willReturn(updatedUserDto);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("updatedUser"))
                .andExpect(jsonPath("$.data.email").value("updated@test.com"))
                .andDo(document("user-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("userId").description("사용자 ID")
                        ),
                        requestFields(
                                fieldWithPath("username").description("사용자명").optional(),
                                fieldWithPath("password").description("비밀번호").optional(),
                                fieldWithPath("email").description("이메일").optional(),
                                fieldWithPath("birth").description("생년월일").optional(),
                                fieldWithPath("phoneNumber").description("전화번호").optional(),
                                fieldWithPath("gender").description("성별").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("사용자 ID"),
                                fieldWithPath("data.username").description("사용자명"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.birth").description("생년월일"),
                                fieldWithPath("data.phoneNumber").description("전화번호"),
                                fieldWithPath("data.gender").description("성별"),
                                fieldWithPath("data.socialType").description("소셜 타입"),
                                fieldWithPath("data.socialId").description("소셜 ID").optional()
                        )
                ));

        assertThat(updatedUserDto).isNotNull();
        assertThat(updatedUserDto.getUsername()).isEqualTo("updatedUser");
        then(userService).should().updateUserInfo(eq(userId), any(UserUpdateRequestDto.class));
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getUserProfile_Success() throws Exception {
        // given
        given(userService.getUserProfile(any(User.class), eq(userId))).willReturn(userProfileDto);

        // when & then
        mockMvc.perform(get("/api/users/{userId}/profiles", userId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.data.introduction").value("안녕하세요"))
                .andDo(document("user-profile-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("userId").description("사용자 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("사용자 ID"),
                                fieldWithPath("data.username").description("사용자명"),
                                fieldWithPath("data.nickname").description("닉네임"),
                                fieldWithPath("data.introduction").description("소개"),
                                fieldWithPath("data.websiteUrl").description("웹사이트 URL"),
                                fieldWithPath("data.profileImageUrl").description("프로필 이미지 URL"),
                                fieldWithPath("data.postCount").description("게시물 수"),
                                fieldWithPath("data.followerCount").description("팔로워 수"),
                                fieldWithPath("data.followingCount").description("팔로잉 수"),
                                fieldWithPath("data.following").description("팔로잉 여부")
                        )
                ));

        assertThat(userProfileDto).isNotNull();
        assertThat(userProfileDto.getNickname()).isEqualTo("테스트유저");
        then(userService).should().getUserProfile(any(User.class), eq(userId));
    }

    @Test
    @DisplayName("사용자 프로필 수정 성공")
    void updateUserProfile_Success() throws Exception {
        // given
        UserProfileUpdateRequestDto updateRequestDto = UserProfileUpdateRequestDto.builder()
                .nickname("수정된닉네임")
                .profileImageUrl("http://example.com/updated.jpg")
                .introduction("수정된 소개")
                .websiteUrl("http://updated.com")
                .build();

        UserProfileDto updatedProfileDto = UserProfileDto.builder()
                .id(userId)
                .username("testUser")
                .nickname("수정된닉네임")
                .introduction("수정된 소개")
                .websiteUrl("http://updated.com")
                .profileImageUrl("http://example.com/updated.jpg")
                .postCount(10L)
                .followerCount(5L)
                .followingCount(3L)
                .following(false)
                .build();

        given(userService.updateUserProfile(any(User.class), eq(userId), any(UserProfileUpdateRequestDto.class)))
                .willReturn(updatedProfileDto);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/profiles", userId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("수정된닉네임"))
                .andExpect(jsonPath("$.data.introduction").value("수정된 소개"))
                .andDo(document("user-profile-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("userId").description("사용자 ID")
                        ),
                        requestFields(
                                fieldWithPath("nickname").description("닉네임").optional(),
                                fieldWithPath("profileImageUrl").description("프로필 이미지 URL").optional(),
                                fieldWithPath("introduction").description("소개").optional(),
                                fieldWithPath("websiteUrl").description("웹사이트 URL").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.id").description("사용자 ID"),
                                fieldWithPath("data.username").description("사용자명"),
                                fieldWithPath("data.nickname").description("닉네임"),
                                fieldWithPath("data.introduction").description("소개"),
                                fieldWithPath("data.websiteUrl").description("웹사이트 URL"),
                                fieldWithPath("data.profileImageUrl").description("프로필 이미지 URL"),
                                fieldWithPath("data.postCount").description("게시물 수"),
                                fieldWithPath("data.followerCount").description("팔로워 수"),
                                fieldWithPath("data.followingCount").description("팔로잉 수"),
                                fieldWithPath("data.following").description("팔로잉 여부")
                        )
                ));

        assertThat(updatedProfileDto).isNotNull();
        assertThat(updatedProfileDto.getNickname()).isEqualTo("수정된닉네임");
        then(userService).should().updateUserProfile(any(User.class), eq(userId), any(UserProfileUpdateRequestDto.class));
    }

    @Test
    @DisplayName("팔로우 요청 성공")
    void followUser_Success() throws Exception {
        // given
        String targetUsername = "targetUser";
        willDoNothing().given(userService).followUser(any(User.class), eq(targetUsername));

        // when & then
        mockMvc.perform(post("/api/users/following/{username}", targetUsername)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("팔로우 요청이 성공적으로 처리되었습니다."))
                .andDo(document("user-follow",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("username").description("팔로우할 사용자명")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data").description("결과 메시지")
                        )
                ));

        then(userService).should().followUser(any(User.class), eq(targetUsername));
    }

    @Test
    @DisplayName("언팔로우 요청 성공")
    void unfollowUser_Success() throws Exception {
        // given
        String targetUsername = "targetUser";
        willDoNothing().given(userService).unfollowUser(any(User.class), eq(targetUsername));

        // when & then
        mockMvc.perform(post("/api/users/unfollow/{username}", targetUsername)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("언팔로우 요청이 성공적으로 처리되었습니다."))
                .andDo(document("user-unfollow",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("username").description("언팔로우할 사용자명")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data").description("결과 메시지")
                        )
                ));

        then(userService).should().unfollowUser(any(User.class), eq(targetUsername));
    }

    @Test
    @DisplayName("특정 사용자의 팔로워 목록 조회 성공")
    void getUserFollowers_Success() throws Exception {
        // given
        String targetUsername = "targetUser";
        FollowDto followDto = FollowDto.builder()
                .id(2L)
                .username("follower1")
                .nickname("팔로워1")
                .profileImageUrl("http://example.com/follower1.jpg")
                .following(true)
                .build();

        CursorPageResponseDto<FollowDto> response = CursorPageResponseDto.<FollowDto>builder()
                .content(List.of(followDto))
                .hasNext(false)
                .nextCursor(null)
                .build();

        given(userService.getUserFollowers(
                any(User.class),
                eq(targetUsername),
                anyString(),
                eq("createdAt"),
                eq("ASC"),
                anyString(),
                anyLong(),
                eq(10)
        )).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/{username}/followers", targetUsername)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "test")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("cursor", "cursor1")
                        .param("after", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].username").value("follower1"))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("user-followers-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("username").description("사용자명")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서").optional(),
                                parameterWithName("after").description("다음 페이지 시작점").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content[].id").description("팔로워 ID"),
                                fieldWithPath("data.content[].username").description("팔로워 사용자명"),
                                fieldWithPath("data.content[].nickname").description("팔로워 닉네임"),
                                fieldWithPath("data.content[].profileImageUrl").description("팔로워 프로필 이미지 URL"),
                                fieldWithPath("data.content[].following").description("팔로잉 여부"),
                                fieldWithPath("data.nextCursor").description("다음 커서").optional(),
                                fieldWithPath("data.nextAfter").description("다음 After ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        assertThat(response.getContent()).hasSize(1);
        then(userService).should().getUserFollowers(
                any(User.class),
                eq(targetUsername),
                anyString(),
                eq("createdAt"),
                eq("ASC"),
                anyString(),
                anyLong(),
                eq(10)
        );
    }

    @Test
    @DisplayName("특정 사용자의 팔로잉 목록 조회 성공")
    void getUserFollowing_Success() throws Exception {
        // given
        String targetUsername = "targetUser";
        FollowDto followDto = FollowDto.builder()
                .id(3L)
                .username("following1")
                .nickname("팔로잉1")
                .profileImageUrl("http://example.com/following1.jpg")
                .following(false)
                .build();

        CursorPageResponseDto<FollowDto> response = CursorPageResponseDto.<FollowDto>builder()
                .content(List.of(followDto))
                .hasNext(false)
                .nextCursor(null)
                .build();

        given(userService.getUserFollowing(
                any(User.class),
                eq(targetUsername),
                anyString(),
                eq("createdAt"),
                eq("ASC"),
                anyString(),
                anyLong(),
                eq(10)
        )).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/{username}/following", targetUsername)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "test")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("cursor", "cursor1")
                        .param("after", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].username").value("following1"))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("user-following-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("username").description("사용자명")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서").optional(),
                                parameterWithName("after").description("다음 페이지 시작점").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content[].id").description("팔로잉 사용자 ID"),
                                fieldWithPath("data.content[].username").description("팔로잉 사용자명"),
                                fieldWithPath("data.content[].nickname").description("팔로잉 사용자 닉네임"),
                                fieldWithPath("data.content[].profileImageUrl").description("팔로잉 사용자 프로필 이미지 URL"),
                                fieldWithPath("data.content[].following").description("팔로잉 여부"),
                                fieldWithPath("data.nextCursor").description("다음 커서").optional(),
                                fieldWithPath("data.nextAfter").description("다음 After ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        assertThat(response.getContent()).hasSize(1);
        then(userService).should().getUserFollowing(
                any(User.class),
                eq(targetUsername),
                anyString(),
                eq("createdAt"),
                eq("ASC"),
                anyString(),
                anyLong(),
                eq(10)
        );
    }

    @Test
    @DisplayName("특정 사용자가 게시한 게시글 목록 조회 성공")
    void getUserPosts_Success() throws Exception {
        // given
        String targetUsername = "targetUser";
        PostDto postDto = PostDto.builder()
                .id(1L)
                .authorProfileImageUrl("http://example.com/target.jpg")
                .content("테스트 내용")
                .imageUrls(List.of("http://example.com/image1.jpg"))
                .hashTags(List.of("#여행", "#맛집"))
                .numberOfLikes(10L)
                .numberOfComments(5L)
                .liked(false)
                .bookmarked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CursorPageResponseDto<PostDto> response = CursorPageResponseDto.<PostDto>builder()
                .content(List.of(postDto))
                .hasNext(false)
                .nextCursor(null)
                .build();

        given(postService.getUserPosts(
                any(User.class),
                eq(targetUsername),
                anyString(),
                eq("createdAt"),
                eq("ASC"),
                anyString(),
                anyLong(),
                eq(10)
        )).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/{username}/posts", targetUsername)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "test")
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("cursor", "cursor1")
                        .param("after", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].content").value("테스트 내용"))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(document("user-posts-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("username").description("사용자명")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("orderBy").description("정렬 기준 (기본값: createdAt)").optional(),
                                parameterWithName("direction").description("정렬 방향 (ASC, DESC, 기본값: ASC)").optional(),
                                parameterWithName("cursor").description("커서").optional(),
                                parameterWithName("after").description("다음 페이지 시작점").optional(),
                                parameterWithName("limit").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content[].id").description("게시글 ID"),
                                fieldWithPath("data.content[].authorProfileImageUrl").description("작성자 프로필 이미지 URL"),
                                fieldWithPath("data.content[].content").description("게시글 내용"),
                                fieldWithPath("data.content[].imageUrls").description("게시글 이미지 URL 목록"),
                                fieldWithPath("data.content[].hashTags").description("해시태그 목록"),
                                fieldWithPath("data.content[].numberOfLikes").description("좋아요 수"),
                                fieldWithPath("data.content[].numberOfComments").description("댓글 수"),
                                fieldWithPath("data.content[].liked").description("좋아요 여부"),
                                fieldWithPath("data.content[].bookmarked").description("북마크 여부"),
                                fieldWithPath("data.content[].createdAt").description("작성일시"),
                                fieldWithPath("data.content[].updatedAt").description("수정일시"),
                                fieldWithPath("data.nextCursor").description("다음 커서"),
                                fieldWithPath("data.nextAfter").description("다음 After ID"),
                                fieldWithPath("data.size").description("현재 페이지 크기"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부")
                        )
                ));

        assertThat(response.getContent()).hasSize(1);
        then(postService).should().getUserPosts(
                any(User.class),
                eq(targetUsername),
                anyString(),
                eq("createdAt"),
                eq("ASC"),
                anyString(),
                anyLong(),
                eq(10)
        );
    }
}

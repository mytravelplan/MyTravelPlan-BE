package travel.mytravelplan.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.post.dto.PostDto;
import travel.mytravelplan.domain.post.service.PostService;
import travel.mytravelplan.domain.user.dto.*;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.service.UserService;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PostService postService;

    // 회원가입
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody @Validated UserCreateRequestDto userCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.join(userCreateRequestDto)));
    }

    // 사용자 정보 조회
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id)")
    public ResponseEntity<ApiResponse<UserDto>> getUserInfo(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserInfo(userId)));
    }

    // 사용자 정보 수정
    @PatchMapping("/{userId}")
    @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserInfo(@PathVariable Long userId, @RequestBody @Validated UserUpdateRequestDto userUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserInfo(userId, userUpdateRequestDto)));
    }

    // 사용자 프로필 조회
    @GetMapping("/{userId}/profiles")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@LoginUser User currentUser, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(currentUser, userId)));
    }

    // 사용자 프로필 수정
    @PatchMapping("/{userId}/profiles")
    @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id)")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserProfile(@LoginUser User currentUser, @PathVariable Long userId, @RequestBody @Validated UserProfileUpdateRequestDto userProfileUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserProfile(currentUser, userId, userProfileUpdateRequestDto)));
    }

    // 팔로우 요청
    @PostMapping("/following/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> followUser(@LoginUser User currentUser, @PathVariable String username) {
        userService.followUser(currentUser, username);
        return ResponseEntity.ok(ApiResponse.success("팔로우 요청이 성공적으로 처리되었습니다."));
    }

    // 언팔로우 요청
    @PostMapping("/unfollow/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> unfollowUser(@LoginUser User currentUser, @PathVariable String username) {
        userService.unfollowUser(currentUser, username);
        return ResponseEntity.ok(ApiResponse.success("언팔로우 요청이 성공적으로 처리되었습니다."));
    }

    // 특정 사용자의 팔로워 목록 조회
    @GetMapping("/{username}/followers")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<FollowDto>>> getUserFollowers(
            @LoginUser User currentUser,
            @PathVariable String username,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserFollowers(currentUser, username, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 특정 사용자의 팔로잉 목록 조회
    @GetMapping("/{username}/following")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<FollowDto>>> getUserFollowing(
            @LoginUser User currentUser,
            @PathVariable String username,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserFollowing(currentUser, username, keyword, orderBy, direction, cursor, after, limit)));
    }

    // 특정 사용자가 작성한 게시글 목록 조회
    @GetMapping("/{username}/posts")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<PostDto>>> getUserPosts(
            @LoginUser User currentUser,
            @PathVariable String username,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getUserPosts(currentUser, username, keyword, orderBy, direction, cursor, after, limit)));
    }
}

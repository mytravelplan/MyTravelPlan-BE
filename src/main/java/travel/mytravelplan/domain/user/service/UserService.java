package travel.mytravelplan.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.user.dto.*;
import travel.mytravelplan.domain.user.entity.Follow;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.entity.UserProfile;
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

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final FollowMapper followMapper;

    @Transactional
    public UserDto join(UserCreateRequestDto userCreateRequestDto) {
        userRepository.findByUsername(userCreateRequestDto.getUsername()).ifPresent(user -> {
            throw new UserException(UserErrorCode.DUPLICATE_USER);
        });

        UserProfile userProfile = UserProfile.createUserProfile(userCreateRequestDto.getNickname(), userCreateRequestDto.getProfileImageUrl());

        User user = User.createUser(
                userCreateRequestDto.getUsername(),
                passwordEncoder.encode(userCreateRequestDto.getPassword()),
                userCreateRequestDto.getEmail(),
                SocialType.LOCAL,
                null,
                userCreateRequestDto.getBirth(),
                userCreateRequestDto.getPhoneNumber(),
                userCreateRequestDto.getGender(),
                userCreateRequestDto.getRoles()
        );

        user.setUserProfile(userProfile);

        userProfileRepository.save(userProfile);

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public UserDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUserInfo(Long userId, UserUpdateRequestDto userUpdateRequestDto) {
        userRepository.findByUsername(userUpdateRequestDto.getUsername()).ifPresent(user -> {
            throw new UserException(UserErrorCode.DUPLICATE_USER);
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.updateInfo(
                userUpdateRequestDto.getUsername(),
                passwordEncoder.encode(userUpdateRequestDto.getPassword()),
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getBirth(),
                userUpdateRequestDto.getPhoneNumber(),
                userUpdateRequestDto.getGender()
        );

        return userMapper.toDto(user);
    }

    public UserProfileDto getUserProfile(User currentUser, Long userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return userProfileMapper.toDto(targetUser, isFollowing(currentUser, targetUser));
    }

    @Transactional
    public UserProfileDto updateUserProfile(User currentUser, Long userId, UserProfileUpdateRequestDto userProfileUpdateRequestDto) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        targetUser.updateProfile(
                userProfileUpdateRequestDto.getNickname(),
                userProfileUpdateRequestDto.getProfileImageUrl(),
                userProfileUpdateRequestDto.getIntroduction(),
                userProfileUpdateRequestDto.getWebsiteUrl()
        );

        return userProfileMapper.toDto(targetUser, isFollowing(currentUser, targetUser));
    }

    @Transactional
    public void followUser(User currentUser, String username) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new UserException(UserErrorCode.USER_CANNOT_FOLLOW_SELF);
        }

        if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            throw new UserException(UserErrorCode.USER_ALREADY_FOLLOWING);
        }

        Follow follow = Follow.createFollow(currentUser, targetUser);

        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(User currentUser, String username) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new UserException(UserErrorCode.USER_CANNOT_FOLLOW_SELF);
        }

        followRepository.deleteByFollowerAndFollowing(currentUser, targetUser);
    }

    public CursorPageResponseDto<FollowDto> getUserFollowers(User currentUser, String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        List<Follow> follows = followRepository.findAllFollowers(user.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = follows.size() > limit;

        List<Follow> pagedFollows = hasNext ? follows.subList(0, limit) : follows;

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Follow lastFollow = pagedFollows.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastFollow.getCreatedAt().toString();
            }

            nextAfter = lastFollow.getId();
        }

        return CursorPageResponseDto.<FollowDto>builder()
                .content(pagedFollows.stream()
                        .map(Follow::getFollower)
                        .map(targetUser -> followMapper.toDto(targetUser, isFollowing(targetUser, currentUser)))
                        .toList())
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(pagedFollows.size())
                .hasNext(hasNext)
                .build();
    }

    public CursorPageResponseDto<FollowDto> getUserFollowing(User currentUser, String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        List<Follow> follows = followRepository.findAllFollowings(user.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = follows.size() > limit;

        List<Follow> pagedFollows = hasNext ? follows.subList(0, limit) : follows;

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Follow lastFollow = pagedFollows.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastFollow.getCreatedAt().toString();
            }

            nextAfter = lastFollow.getId();
        }

        return CursorPageResponseDto.<FollowDto>builder()
                .content(pagedFollows.stream()
                        .map(Follow::getFollowing)
                        .map(targetUser -> followMapper.toDto(targetUser, isFollowing(currentUser, targetUser)))
                        .toList())
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(pagedFollows.size())
                .hasNext(hasNext)
                .build();
    }

    private boolean isFollowing(User follower, User following) {
        return followRepository.existsByFollowerAndFollowing(follower, following);
    }
}

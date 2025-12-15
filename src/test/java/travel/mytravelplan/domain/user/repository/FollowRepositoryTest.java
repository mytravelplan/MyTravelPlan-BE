package travel.mytravelplan.domain.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.user.entity.Follow;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("팔로우 레포지토리 테스트")
class FollowRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("팔로우를 저장한다")
    void saveFollow() {
        // given
        User follower = createUser("follower", "follower@email.com");
        User following = createUser("following", "following@email.com");
        Follow follow = createFollow(follower, following);

        // when
        Follow savedFollow = followRepository.save(follow);
        em.flush();
        em.clear();

        // then
        assertThat(savedFollow.getId()).isNotNull();
        assertThat(savedFollow.getFollower().getId()).isEqualTo(follower.getId());
        assertThat(savedFollow.getFollowing().getId()).isEqualTo(following.getId());
    }

    @Test
    @DisplayName("팔로우를 ID로 조회한다")
    void findFollowById() {
        // given
        User follower = createUser("follower", "follower@email.com");
        User following = createUser("following", "following@email.com");
        Follow follow = createAndSaveFollow(follower, following);
        em.flush();
        em.clear();

        // when
        Follow foundFollow = followRepository.findById(follow.getId()).orElse(null);

        // then
        assertThat(foundFollow).isNotNull();
        assertThat(foundFollow.getId()).isEqualTo(follow.getId());
        assertThat(foundFollow.getFollower().getId()).isEqualTo(follower.getId());
        assertThat(foundFollow.getFollowing().getId()).isEqualTo(following.getId());
    }

    @Test
    @DisplayName("팔로우 관계가 존재하는지 확인한다")
    void existsByFollowerAndFollowing() {
        // given
        User follower = createUser("follower", "follower@email.com");
        User following = createUser("following", "following@email.com");
        createAndSaveFollow(follower, following);
        em.flush();
        em.clear();

        // when
        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("팔로우 관계가 존재하지 않으면 false를 반환한다")
    void existsByFollowerAndFollowing_notExists() {
        // given
        User follower = createUser("follower", "follower@email.com");
        User following = createUser("following", "following@email.com");

        // when
        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("팔로우 관계를 삭제한다")
    void deleteByFollowerAndFollowing() {
        // given
        User follower = createUser("follower", "follower@email.com");
        User following = createUser("following", "following@email.com");
        createAndSaveFollow(follower, following);
        em.flush();
        em.clear();

        // when
        followRepository.deleteByFollowerAndFollowing(follower, following);
        em.flush();
        em.clear();

        // then
        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 사용자의 팔로워 수를 조회한다")
    void countByFollowingId() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");
        User follower3 = createUser("follower3", "follower3@email.com");

        createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        createAndSaveFollow(follower3, targetUser);
        em.flush();
        em.clear();

        // when
        Long count = followRepository.countByFollowingId(targetUser.getId());

        // then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("특정 사용자의 팔로잉 수를 조회한다")
    void countByFollowerId() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");

        createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        em.flush();
        em.clear();

        // when
        Long count = followRepository.countByFollowerId(targetUser.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("특정 사용자의 팔로잉 목록을 조회한다")
    void findAllFollowings() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");
        User following3 = createUser("following3", "following3@email.com");

        createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        createAndSaveFollow(targetUser, following3);
        em.flush();
        em.clear();

        // when
        List<Follow> followings = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(followings).hasSize(3);
        assertThat(followings)
                .extracting(follow -> follow.getFollowing().getUsername())
                .containsExactly("following3", "following2", "following1");
    }

    @Test
    @DisplayName("특정 사용자의 팔로워 목록을 조회한다")
    void findAllFollowers() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");
        User follower3 = createUser("follower3", "follower3@email.com");

        createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        createAndSaveFollow(follower3, targetUser);
        em.flush();
        em.clear();

        // when
        List<Follow> followers = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(followers).hasSize(3);
        assertThat(followers)
                .extracting(follow -> follow.getFollower().getUsername())
                .containsExactly("follower3", "follower2", "follower1");
    }

    @Test
    @DisplayName("키워드로 팔로잉 목록을 검색한다")
    void findAllFollowings_byKeyword() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("john_doe", "john@email.com");
        User following2 = createUser("jane_smith", "jane@email.com");
        User following3 = createUser("john_smith", "johnsmith@email.com");

        createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        createAndSaveFollow(targetUser, following3);
        em.flush();
        em.clear();

        // when
        List<Follow> followings = followRepository.findAllFollowings(
                targetUser.getId(),
                "john",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(followings).hasSize(2);
        assertThat(followings)
                .extracting(follow -> follow.getFollowing().getUsername())
                .containsExactlyInAnyOrder("john_doe", "john_smith");
    }

    @Test
    @DisplayName("키워드로 팔로워 목록을 검색한다")
    void findAllFollowers_byKeyword() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("alice_kim", "alice@email.com");
        User follower2 = createUser("bob_park", "bob@email.com");
        User follower3 = createUser("alice_lee", "alicelee@email.com");

        createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        createAndSaveFollow(follower3, targetUser);
        em.flush();
        em.clear();

        // when
        List<Follow> followers = followRepository.findAllFollowers(
                targetUser.getId(),
                "alice",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(followers).hasSize(2);
        assertThat(followers)
                .extracting(follow -> follow.getFollower().getUsername())
                .containsExactlyInAnyOrder("alice_kim", "alice_lee");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 팔로잉 목록을 조회한다")
    void findAllFollowings_orderByCreatedAtAsc() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");
        User following3 = createUser("following3", "following3@email.com");

        createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        createAndSaveFollow(targetUser, following3);
        em.flush();
        em.clear();

        // when
        List<Follow> followings = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(followings).hasSize(3);
        assertThat(followings)
                .extracting(follow -> follow.getFollowing().getUsername())
                .containsExactly("following1", "following2", "following3");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 팔로워 목록을 조회한다")
    void findAllFollowers_orderByCreatedAtAsc() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");
        User follower3 = createUser("follower3", "follower3@email.com");

        createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        createAndSaveFollow(follower3, targetUser);
        em.flush();
        em.clear();

        // when
        List<Follow> followers = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(followers).hasSize(3);
        assertThat(followers)
                .extracting(follow -> follow.getFollower().getUsername())
                .containsExactly("follower1", "follower2", "follower3");
    }

    @Test
    @DisplayName("limit 개수만큼 팔로잉 목록을 조회한다")
    void findAllFollowings_withLimit() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");
        User following3 = createUser("following3", "following3@email.com");
        User following4 = createUser("following4", "following4@email.com");
        User following5 = createUser("following5", "following5@email.com");

        createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        createAndSaveFollow(targetUser, following3);
        createAndSaveFollow(targetUser, following4);
        createAndSaveFollow(targetUser, following5);
        em.flush();
        em.clear();

        // when
        List<Follow> followings = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(followings).hasSize(3);
    }

    @Test
    @DisplayName("limit 개수만큼 팔로워 목록을 조회한다")
    void findAllFollowers_withLimit() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");
        User follower3 = createUser("follower3", "follower3@email.com");
        User follower4 = createUser("follower4", "follower4@email.com");
        User follower5 = createUser("follower5", "follower5@email.com");

        createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        createAndSaveFollow(follower3, targetUser);
        createAndSaveFollow(follower4, targetUser);
        createAndSaveFollow(follower5, targetUser);
        em.flush();
        em.clear();

        // when
        List<Follow> followers = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(followers).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 팔로잉 목록을 조회한다 - 내림차순")
    void findAllFollowings_withCursor_desc() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");
        User following3 = createUser("following3", "following3@email.com");

        createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        createAndSaveFollow(targetUser, following3);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Follow> firstPage = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getFollowing().getUsername()).isEqualTo("following3");
        assertThat(firstPage.get(1).getFollowing().getUsername()).isEqualTo("following2");

        // when - 두 번째 페이지 조회
        Follow lastFollow = firstPage.get(firstPage.size() - 1);
        List<Follow> secondPage = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "desc",
                lastFollow.getCreatedAt().toString(),
                lastFollow.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getFollowing().getUsername()).isEqualTo("following1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 팔로잉 목록을 조회한다 - 오름차순")
    void findAllFollowings_withCursor_asc() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");
        User following3 = createUser("following3", "following3@email.com");

        createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        createAndSaveFollow(targetUser, following3);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Follow> firstPage = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getFollowing().getUsername()).isEqualTo("following1");
        assertThat(firstPage.get(1).getFollowing().getUsername()).isEqualTo("following2");

        // when - 두 번째 페이지 조회
        Follow lastFollow = firstPage.get(firstPage.size() - 1);
        List<Follow> secondPage = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                lastFollow.getCreatedAt().toString(),
                lastFollow.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getFollowing().getUsername()).isEqualTo("following3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 팔로워 목록을 조회한다 - 내림차순")
    void findAllFollowers_withCursor_desc() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");
        User follower3 = createUser("follower3", "follower3@email.com");

        createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        createAndSaveFollow(follower3, targetUser);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Follow> firstPage = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getFollower().getUsername()).isEqualTo("follower3");
        assertThat(firstPage.get(1).getFollower().getUsername()).isEqualTo("follower2");

        // when - 두 번째 페이지 조회
        Follow lastFollow = firstPage.get(firstPage.size() - 1);
        List<Follow> secondPage = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "desc",
                lastFollow.getCreatedAt().toString(),
                lastFollow.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getFollower().getUsername()).isEqualTo("follower1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 팔로워 목록을 조회한다 - 오름차순")
    void findAllFollowers_withCursor_asc() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");
        User follower3 = createUser("follower3", "follower3@email.com");

        createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        createAndSaveFollow(follower3, targetUser);
        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Follow> firstPage = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getFollower().getUsername()).isEqualTo("follower1");
        assertThat(firstPage.get(1).getFollower().getUsername()).isEqualTo("follower2");

        // when - 두 번째 페이지 조회
        Follow lastFollow = firstPage.get(firstPage.size() - 1);
        List<Follow> secondPage = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                lastFollow.getCreatedAt().toString(),
                lastFollow.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getFollower().getUsername()).isEqualTo("follower3");
    }

    @Test
    @DisplayName("팔로잉 목록 조회 시 cursor가 null이지만 after가 있는 경우 - after는 무시된다")
    void findAllFollowings_withNullCursorButAfterExists() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");

        createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        em.flush();
        em.clear();

        // when
        List<Follow> followings = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                null,
                999L,
                10
        );

        // then - cursor가 null이므로 모든 데이터 조회
        assertThat(followings).hasSize(2);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 시 cursor만 있고 after가 null인 경우 - 조건이 적용되지 않는다")
    void findAllFollowings_withCursorButNullAfter() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");

        Follow follow1 = createAndSaveFollow(targetUser, following1);
        createAndSaveFollow(targetUser, following2);
        em.flush();
        em.clear();

        // when
        List<Follow> followings = followRepository.findAllFollowings(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                follow1.getCreatedAt().toString(),
                null,
                10
        );

        // then - after가 null이므로 모든 데이터 조회
        assertThat(followings).hasSize(2);
    }

    @Test
    @DisplayName("팔로워 목록 조회 시 cursor가 null이지만 after가 있는 경우 - after는 무시된다")
    void findAllFollowers_withNullCursorButAfterExists() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");

        createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        em.flush();
        em.clear();

        // when
        List<Follow> followers = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                null,
                999L,
                10
        );

        // then - cursor가 null이므로 모든 데이터 조회
        assertThat(followers).hasSize(2);
    }

    @Test
    @DisplayName("팔로워 목록 조회 시 cursor만 있고 after가 null인 경우 - 조건이 적용되지 않는다")
    void findAllFollowers_withCursorButNullAfter() {
        // given
        User targetUser = createUser("target", "target@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");

        Follow follow1 = createAndSaveFollow(follower1, targetUser);
        createAndSaveFollow(follower2, targetUser);
        em.flush();
        em.clear();

        // when
        List<Follow> followers = followRepository.findAllFollowers(
                targetUser.getId(),
                null,
                "createdAt",
                "asc",
                follow1.getCreatedAt().toString(),
                null,
                10
        );

        // then - after가 null이므로 모든 데이터 조회
        assertThat(followers).hasSize(2);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 시 userId가 null인 경우 - 모든 팔로우 관계를 조회한다")
    void findAllFollowings_withNullUserId() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        User following1 = createUser("following1", "following1@email.com");
        User following2 = createUser("following2", "following2@email.com");

        createAndSaveFollow(user1, following1);
        createAndSaveFollow(user1, following2);
        createAndSaveFollow(user2, following1);
        em.flush();
        em.clear();

        // when
        List<Follow> followings = followRepository.findAllFollowings(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - userId가 null이므로 모든 팔로우 관계 조회
        assertThat(followings).hasSize(3);
    }

    @Test
    @DisplayName("팔로워 목록 조회 시 userId가 null인 경우 - 모든 팔로우 관계를 조회한다")
    void findAllFollowers_withNullUserId() {
        // given
        User targetUser1 = createUser("target1", "target1@email.com");
        User targetUser2 = createUser("target2", "target2@email.com");
        User follower1 = createUser("follower1", "follower1@email.com");
        User follower2 = createUser("follower2", "follower2@email.com");

        createAndSaveFollow(follower1, targetUser1);
        createAndSaveFollow(follower2, targetUser1);
        createAndSaveFollow(follower1, targetUser2);
        em.flush();
        em.clear();

        // when
        List<Follow> followers = followRepository.findAllFollowers(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then - userId가 null이므로 모든 팔로우 관계 조회
        assertThat(followers).hasSize(3);
    }

    // TestFixture 메서드들
    private User createUser(String username, String email) {
        User user = User.createUser(
                username,
                "password123",
                email,
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );
        return userRepository.save(user);
    }

    private Follow createFollow(User follower, User following) {
        return Follow.createFollow(follower, following);
    }

    private Follow createAndSaveFollow(User follower, User following) {
        Follow follow = Follow.createFollow(follower, following);
        return followRepository.save(follow);
    }
}
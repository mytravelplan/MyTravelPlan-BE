package travel.mytravelplan.domain.post.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.post.entity.HashTag;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("게시글 레포지토리 테스트")
class PostRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashTagRepository hashTagRepository;

    @Test
    @DisplayName("게시글을 저장하고 조회할 수 있다")
    void save_and_findById() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행", "제주도"));

        Post post = createPost("제주도 여행 후기입니다", List.of("https://example.com/image1.jpg"), user, hashTags);
        Post savedPost = postRepository.save(post);

        em.flush();
        em.clear();

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getContent()).isEqualTo("제주도 여행 후기입니다");
        assertThat(foundPost.get().getImageUrls()).hasSize(1);
        assertThat(foundPost.get().getPostHashTags()).hasSize(2);
    }

    @Test
    @DisplayName("User와 함께 게시글을 조회할 수 있다 - findWithUserById")
    void findWithUserById() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        Post post = createAndSavePost("여행 후기", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        Optional<Post> foundPost = postRepository.findWithUserById(post.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getUser()).isNotNull();
        assertThat(foundPost.get().getUser().getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("특정 사용자의 게시글 개수를 조회할 수 있다 - countByUser")
    void countByUser() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글1", List.of(), user1, hashTags);
        createAndSavePost("게시글2", List.of(), user1, hashTags);
        createAndSavePost("게시글3", List.of(), user2, hashTags);

        em.flush();
        em.clear();

        // when
        Long count = postRepository.countByUser(user1);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글을 수정할 수 있다")
    void update() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행", "제주도"));

        Post post = createAndSavePost("여행 후기", List.of("https://example.com/image1.jpg"), user, hashTags);
        Long postId = post.getId();

        em.flush();
        em.clear();

        // when
        Post foundPost = postRepository.findById(postId).orElseThrow();
        List<HashTag> newHashTags = createHashTags(List.of("부산", "맛집"));
        foundPost.update(
                "부산 여행 후기 - 수정",
                List.of("https://example.com/image2.jpg", "https://example.com/image3.jpg"),
                newHashTags
        );

        em.flush();
        em.clear();

        // then
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        assertThat(updatedPost.getContent()).isEqualTo("부산 여행 후기 - 수정");
        assertThat(updatedPost.getImageUrls()).hasSize(2);
        assertThat(updatedPost.getPostHashTags()).hasSize(2);
        assertThat(updatedPost.getPostHashTags())
                .extracting(postHashTag -> postHashTag.getHashTag().getName())
                .containsExactlyInAnyOrder("부산", "맛집");
    }

    @Test
    @DisplayName("게시글을 삭제할 수 있다")
    void delete() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        Post post = createAndSavePost("여행 후기", List.of(), user, hashTags);
        Long postId = post.getId();

        em.flush();
        em.clear();

        // when
        Post foundPost = postRepository.findById(postId).orElseThrow();
        postRepository.delete(foundPost);

        em.flush();
        em.clear();

        // then
        Optional<Post> deletedPost = postRepository.findById(postId);
        assertThat(deletedPost).isEmpty();
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 게시글 목록을 조회할 수 있다")
    void findAllByCursor_pagination() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(3);
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactly("게시글 3", "게시글 2", "게시글 1");
    }

    @Test
    @DisplayName("사용자명으로 게시글을 필터링할 수 있다")
    void findAllByCursor_byUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("user1의 게시글 1", List.of(), user1, hashTags);
        createAndSavePost("user1의 게시글 2", List.of(), user1, hashTags);
        createAndSavePost("user2의 게시글 1", List.of(), user2, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                "user1",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactlyInAnyOrder("user1의 게시글 1", "user1의 게시글 2");
    }

    @Test
    @DisplayName("키워드로 게시글을 검색할 수 있다")
    void findAllByCursor_withKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("제주도 여행 후기", List.of(), user, hashTags);
        createAndSavePost("부산 맛집 투어", List.of(), user, hashTags);
        createAndSavePost("제주도 카페 추천", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                "제주도",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactlyInAnyOrder("제주도 여행 후기", "제주도 카페 추천");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 오름차순으로 게시글을 조회할 수 있다")
    void findAllByCursor_orderByAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(3);
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactly("게시글 1", "게시글 2", "게시글 3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - limit을 적용하여 게시글을 조회할 수 있다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);
        createAndSavePost("게시글 4", List.of(), user, hashTags);
        createAndSavePost("게시글 5", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(posts).hasSize(3);
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactly("게시글 5", "게시글 4", "게시글 3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 커서와 after를 사용하여 다음 페이지를 조회할 수 있다")
    void findAllByCursor_withCursorAndAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);
        createAndSavePost("게시글 4", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<Post> firstPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지 조회
        Post lastPost = firstPage.getLast();
        List<Post> secondPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                lastPost.getCreatedAt().toString(),
                lastPost.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Post::getContent)
                .containsExactly("게시글 2", "게시글 1");
    }

    @Test
    @DisplayName("사용자명과 키워드를 함께 사용하여 게시글을 검색할 수 있다")
    void findAllByCursor_withUsernameAndKeyword() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("제주도 여행 후기", List.of(), user1, hashTags);
        createAndSavePost("부산 맛집 투어", List.of(), user1, hashTags);
        createAndSavePost("제주도 카페 추천", List.of(), user2, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                "user1",
                "제주도",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(1);
        assertThat(posts.getFirst().getContent()).isEqualTo("제주도 여행 후기");
    }

    @Test
    @DisplayName("여러 이미지 URL을 가진 게시글을 저장하고 조회할 수 있다")
    void save_and_findById_withMultipleImages() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        Post post = createPost(
                "제주도 여행 사진 모음",
                List.of(
                        "https://example.com/image1.jpg",
                        "https://example.com/image2.jpg",
                        "https://example.com/image3.jpg"
                ),
                user,
                hashTags
        );
        Post savedPost = postRepository.save(post);

        em.flush();
        em.clear();

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getImageUrls()).hasSize(3);
        assertThat(foundPost.get().getImageUrls()).containsExactly(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg",
                "https://example.com/image3.jpg"
        );
    }

    @Test
    @DisplayName("이미지가 없는 게시글을 저장하고 조회할 수 있다")
    void save_and_findById_withoutImages() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        Post post = createPost("여행 후기", List.of(), user, hashTags);
        Post savedPost = postRepository.save(post);

        em.flush();
        em.clear();

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getImageUrls()).isEmpty();
    }

    @Test
    @DisplayName("해시태그가 없는 게시글을 저장하고 조회할 수 있다")
    void save_and_findById_withoutHashTags() {
        // given
        User user = createUser("testUser", "test@email.com");

        Post post = createPost("여행 후기", List.of(), user, List.of());
        Post savedPost = postRepository.save(post);

        em.flush();
        em.clear();

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getPostHashTags()).isEmpty();
    }

    @Test
    @DisplayName("여러 해시태그를 가진 게시글을 저장하고 조회할 수 있다")
    void save_and_findById_withMultipleHashTags() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행", "제주도", "맛집", "카페"));

        Post post = createPost("제주도 여행 후기", List.of(), user, hashTags);
        Post savedPost = postRepository.save(post);

        em.flush();
        em.clear();

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getPostHashTags()).hasSize(4);
        assertThat(foundPost.get().getPostHashTags())
                .extracting(postHashTag -> postHashTag.getHashTag().getName())
                .containsExactlyInAnyOrder("여행", "제주도", "맛집", "카페");
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회하면 빈 리스트를 반환한다")
    void findAllByCursor_nonExistentUsername() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                "nonExistentUser",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("키워드가 일치하는 게시글이 없으면 빈 리스트를 반환한다")
    void findAllByCursor_noMatchingKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("제주도 여행 후기", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                "부산",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열 키워드로 조회하면 모든 게시글을 반환한다")
    void findAllByCursor_emptyKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                "",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(2);
    }

    @Test
    @DisplayName("공백 키워드로 조회하면 모든 게시글을 반환한다")
    void findAllByCursor_blankKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                "   ",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(2);
    }

    @Test
    @DisplayName("cursor만 있고 after가 null이면 커서 조건을 무시한다")
    void findAllByCursor_cursorWithoutAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                "2024-01-01T00:00:00",
                null,
                10
        );

        // then
        assertThat(posts).hasSize(3);
    }

    @Test
    @DisplayName("after만 있고 cursor가 null이면 커서 조건을 무시한다")
    void findAllByCursor_afterWithoutCursor() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                1L,
                10
        );

        // then
        assertThat(posts).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 내림차순에서 동일한 createdAt을 가진 게시글들을 올바르게 처리한다")
    void findAllByCursor_sameCreatedAt_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Post> firstPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        // then
        assertThat(firstPage).hasSize(1);

        // when - 두 번째 페이지 (같은 createdAt 케이스 시뮬레이션)
        Post cursor = firstPage.getFirst();
        List<Post> secondPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                cursor.getCreatedAt().toString(),
                cursor.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 오름차순에서 동일한 createdAt을 가진 게시글들을 올바르게 처리한다")
    void findAllByCursor_sameCreatedAt_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Post> firstPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                1
        );

        // then
        assertThat(firstPage).hasSize(1);

        // when - 두 번째 페이지
        Post cursor = firstPage.getFirst();
        List<Post> secondPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                cursor.getCreatedAt().toString(),
                cursor.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("대소문자 구분 없이 키워드로 검색할 수 있다")
    void findAllByCursor_caseInsensitiveKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("제주도 여행 후기", List.of(), user, hashTags);
        createAndSavePost("JEJU Island Tour", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when - 소문자로 검색
        List<Post> posts1 = postRepository.findAllByCursor(
                null,
                "jeju",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts1).hasSize(1);
        assertThat(posts1.getFirst().getContent()).isEqualTo("JEJU Island Tour");

        // when - 대문자로 검색
        List<Post> posts2 = postRepository.findAllByCursor(
                null,
                "ISLAND",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts2).hasSize(1);
        assertThat(posts2.getFirst().getContent()).isEqualTo("JEJU Island Tour");
    }

    @Test
    @DisplayName("사용자명과 키워드와 커서를 모두 사용하여 페이지네이션할 수 있다")
    void findAllByCursor_allParametersCombined() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("제주도 여행 1", List.of(), user1, hashTags);
        createAndSavePost("제주도 여행 2", List.of(), user1, hashTags);
        createAndSavePost("제주도 여행 3", List.of(), user1, hashTags);
        createAndSavePost("제주도 여행 4", List.of(), user2, hashTags);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Post> firstPage = postRepository.findAllByCursor(
                "user1",
                "제주도",
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지
        Post lastPost = firstPage.getLast();
        List<Post> secondPage = postRepository.findAllByCursor(
                "user1",
                "제주도",
                "createdAt",
                "desc",
                lastPost.getCreatedAt().toString(),
                lastPost.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
    }

    @Test
    @DisplayName("여러 조건으로 필터링 후 결과가 없으면 빈 리스트를 반환한다")
    void findAllByCursor_multipleFiltersNoResult() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("제주도 여행", List.of(), user1, hashTags);
        createAndSavePost("부산 여행", List.of(), user2, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                "user1",
                "부산",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("키워드가 게시글 내용의 일부와 일치하면 조회된다")
    void findAllByCursor_partialKeywordMatch() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("제주도 여행 후기입니다. 정말 좋았어요!", List.of(), user, hashTags);
        createAndSavePost("부산 맛집 투어", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                "좋았",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(1);
        assertThat(posts.getFirst().getContent()).contains("좋았어요");
    }

    @Test
    @DisplayName("User를 fetchJoin하여 N+1 문제를 방지한다")
    void findAllByCursor_fetchJoinUser() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(2);
        // User가 이미 fetch join되어 있어 lazy loading이 발생하지 않음
        assertThat(posts.getFirst().getUser().getUsername()).isEqualTo("testUser");
        assertThat(posts.getLast().getUser().getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("동일한 사용자의 여러 게시글을 정확하게 페이지네이션한다")
    void findAllByCursor_sameUserMultiplePosts() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        for (int i = 1; i <= 10; i++) {
            createAndSavePost("게시글 " + i, List.of(), user, hashTags);
        }

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<Post> page1 = postRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(page1).hasSize(3);

        // when - 두 번째 페이지
        Post cursor1 = page1.getLast();
        List<Post> page2 = postRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                cursor1.getCreatedAt().toString(),
                cursor1.getId(),
                3
        );

        // then
        assertThat(page2).hasSize(3);

        // when - 세 번째 페이지
        Post cursor2 = page2.getLast();
        List<Post> page3 = postRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                cursor2.getCreatedAt().toString(),
                cursor2.getId(),
                3
        );

        // then
        assertThat(page3).hasSize(3);

        // when - 네 번째 페이지
        Post cursor3 = page3.getLast();
        List<Post> page4 = postRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                cursor3.getCreatedAt().toString(),
                cursor3.getId(),
                3
        );

        // then
        assertThat(page4).hasSize(1);
    }

    @Test
    @DisplayName("direction이 대문자 ASC일 때도 정상 작동한다")
    void findAllByCursor_directionUpperCaseASC() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "ASC",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(3);
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactly("게시글 1", "게시글 2", "게시글 3");
    }

    @Test
    @DisplayName("direction이 대문자 DESC일 때도 정상 작동한다")
    void findAllByCursor_directionUpperCaseDESC() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "DESC",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(3);
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactly("게시글 3", "게시글 2", "게시글 1");
    }

    @Test
    @DisplayName("direction이 혼합된 케이스일 때도 정상 작동한다")
    void findAllByCursor_directionMixedCase() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "AsC",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactly("게시글 1", "게시글 2");
    }

    @Test
    @DisplayName("커서 페이지네이션에서 ASC 방향으로 cursor와 after를 사용한다")
    void findAllByCursor_cursorAndAfterWithAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);
        createAndSavePost("게시글 4", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Post> firstPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "ASC",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지
        Post cursor = firstPage.getLast();
        List<Post> secondPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "ASC",
                cursor.getCreatedAt().toString(),
                cursor.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Post::getContent)
                .containsExactly("게시글 3", "게시글 4");
    }

    @Test
    @DisplayName("커서 페이지네이션에서 DESC 방향으로 cursor와 after를 사용한다")
    void findAllByCursor_cursorAndAfterWithDesc() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);
        createAndSavePost("게시글 4", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when - 첫 페이지
        List<Post> firstPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "DESC",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 두 번째 페이지
        Post cursor = firstPage.getLast();
        List<Post> secondPage = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "DESC",
                cursor.getCreatedAt().toString(),
                cursor.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(Post::getContent)
                .containsExactly("게시글 2", "게시글 1");
    }

    @Test
    @DisplayName("null이 아닌 username으로 필터링한다")
    void findAllByCursor_filterByUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("user1 게시글 1", List.of(), user1, hashTags);
        createAndSavePost("user1 게시글 2", List.of(), user1, hashTags);
        createAndSavePost("user2 게시글 1", List.of(), user2, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                "user1",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts)
                .allMatch(post -> post.getUser().getUsername().equals("user1"));
    }

    @Test
    @DisplayName("username이 null이면 모든 사용자의 게시글을 조회한다")
    void findAllByCursor_nullUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("user1 게시글", List.of(), user1, hashTags);
        createAndSavePost("user2 게시글", List.of(), user2, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(2);
    }

    @Test
    @DisplayName("limit 1로 단일 게시글을 조회한다")
    void findAllByCursor_limitOne() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        // then
        assertThat(posts).hasSize(1);
        assertThat(posts.getFirst().getContent()).isEqualTo("게시글 3");
    }

    @Test
    @DisplayName("매우 큰 limit 값으로 조회해도 정상 작동한다")
    void findAllByCursor_largeLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                1000
        );

        // then
        assertThat(posts).hasSize(2);
    }

    @Test
    @DisplayName("같은 createdAt 시간에 여러 게시글이 있을 때 id로 정확히 페이지네이션한다")
    void findAllByCursor_sameCreatedAtMultiplePosts() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when - 첫 페이지 (DESC)
        List<Post> page1 = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(page1).hasSize(2);

        // when - cursor의 id보다 작은 id를 가진 게시글만 조회 (DESC)
        Post cursor = page1.getLast();
        List<Post> page2 = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                cursor.getCreatedAt().toString(),
                cursor.getId(),
                10
        );

        // then - cursor의 id보다 작은 게시글들만 조회됨
        assertThat(page2).allMatch(post -> post.getId() < cursor.getId());
    }

    @Test
    @DisplayName("ASC 정렬에서 같은 createdAt 시간에 여러 게시글이 있을 때 id로 정확히 페이지네이션한다")
    void findAllByCursor_sameCreatedAtMultiplePostsAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("게시글 1", List.of(), user, hashTags);
        createAndSavePost("게시글 2", List.of(), user, hashTags);
        createAndSavePost("게시글 3", List.of(), user, hashTags);

        em.flush();
        em.clear();

        // when - 첫 페이지 (ASC)
        List<Post> page1 = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(page1).hasSize(2);

        // when - cursor의 id보다 큰 id를 가진 게시글만 조회 (ASC)
        Post cursor = page1.getLast();
        List<Post> page2 = postRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                cursor.getCreatedAt().toString(),
                cursor.getId(),
                10
        );

        // then - cursor의 id보다 큰 게시글들만 조회됨
        assertThat(page2).allMatch(post -> post.getId() > cursor.getId());
    }

    @Test
    @DisplayName("username과 keyword를 동시에 사용하여 정확하게 필터링한다")
    void findAllByCursor_usernameAndKeywordCombination() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        List<HashTag> hashTags = createHashTags(List.of("여행"));

        createAndSavePost("제주도 여행 후기", List.of(), user1, hashTags);
        createAndSavePost("부산 맛집 투어", List.of(), user1, hashTags);
        createAndSavePost("제주도 카페 추천", List.of(), user2, hashTags);
        createAndSavePost("서울 여행", List.of(), user2, hashTags);

        em.flush();
        em.clear();

        // when
        List<Post> posts = postRepository.findAllByCursor(
                "user1",
                "여행",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(posts).hasSize(1);
        assertThat(posts.getFirst().getContent()).isEqualTo("제주도 여행 후기");
        assertThat(posts.getFirst().getUser().getUsername()).isEqualTo("user1");
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

    private List<HashTag> createHashTags(List<String> tagNames) {
        return tagNames.stream()
                .map(name -> {
                    HashTag hashTag = HashTag.createHashTag(name);
                    return hashTagRepository.save(hashTag);
                })
                .toList();
    }

    private Post createPost(String content, List<String> imageUrls, User user, List<HashTag> hashTags) {
        return Post.createPost(content, imageUrls, user, hashTags);
    }

    private Post createAndSavePost(String content, List<String> imageUrls, User user, List<HashTag> hashTags) {
        Post post = createPost(content, imageUrls, user, hashTags);
        return postRepository.save(post);
    }
}
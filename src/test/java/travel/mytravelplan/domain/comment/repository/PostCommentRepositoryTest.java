package travel.mytravelplan.domain.comment.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.comment.entity.PostComment;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.repository.PostRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("게시물 댓글 레포지토리 테스트")
public class PostCommentRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("댓글을 저장하고 조회할 수 있다")
    void save_and_findById() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);
        PostComment comment = createComment("댓글 내용", post, user);
        PostComment savedComment = postCommentRepository.save(comment);

        em.flush();
        em.clear();

        // when
        Optional<PostComment> foundComment = postCommentRepository.findById(savedComment.getId());

        // then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getContent()).isEqualTo("댓글 내용");
        assertThat(foundComment.get().getPost().getId()).isEqualTo(post.getId());
        assertThat(foundComment.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("특정 게시물의 댓글 개수를 조회할 수 있다")
    void countByPost() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post1 = createPost("게시물 1", user);
        Post post2 = createPost("게시물 2", user);

        createAndSaveComment("댓글 1", post1, user);
        createAndSaveComment("댓글 2", post1, user);
        createAndSaveComment("댓글 3", post1, user);
        createAndSaveComment("댓글 4", post2, user);

        em.flush();
        em.clear();

        // when
        long count = postCommentRepository.countByPost(post1);

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("여러 게시물의 댓글 개수를 한 번에 조회할 수 있다")
    void countCommentsByPostIds() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post1 = createPost("게시물 1", user);
        Post post2 = createPost("게시물 2", user);
        Post post3 = createPost("게시물 3", user);

        createAndSaveComment("댓글 1-1", post1, user);
        createAndSaveComment("댓글 1-2", post1, user);
        createAndSaveComment("댓글 2-1", post2, user);

        em.flush();
        em.clear();

        // when
        List<PostCommentRepository.PostCommentCountProjection> result =
                postCommentRepository.countCommentsByPostIds(
                        List.of(post1.getId(), post2.getId(), post3.getId())
                );

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(PostCommentRepository.PostCommentCountProjection::getPostId)
                .containsExactlyInAnyOrder(post1.getId(), post2.getId());

        PostCommentRepository.PostCommentCountProjection post1Count = result.stream()
                .filter(p -> p.getPostId().equals(post1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(post1Count.getCommentCount()).isEqualTo(2);

        PostCommentRepository.PostCommentCountProjection post2Count = result.stream()
                .filter(p -> p.getPostId().equals(post2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(post2Count.getCommentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글을 수정할 수 있다")
    void update() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);
        PostComment comment = createAndSaveComment("원래 댓글", post, user);
        Long commentId = comment.getId();

        em.flush();
        em.clear();

        // when
        PostComment foundComment = postCommentRepository.findById(commentId).orElseThrow();
        foundComment.update("수정된 댓글");

        em.flush();
        em.clear();

        // then
        PostComment updatedComment = postCommentRepository.findById(commentId).orElseThrow();
        assertThat(updatedComment.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("댓글을 삭제할 수 있다")
    void delete() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);
        PostComment comment = createAndSaveComment("댓글 내용", post, user);
        Long commentId = comment.getId();

        em.flush();
        em.clear();

        // when
        PostComment foundComment = postCommentRepository.findById(commentId).orElseThrow();
        postCommentRepository.delete(foundComment);

        em.flush();
        em.clear();

        // then
        Optional<PostComment> deletedComment = postCommentRepository.findById(commentId);
        assertThat(deletedComment).isEmpty();
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 댓글 목록을 조회할 수 있다")
    void findAllByCursor_pagination() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);
        createAndSaveComment("댓글 3", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments)
                .extracting(PostComment::getContent)
                .containsExactly("댓글 3", "댓글 2", "댓글 1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 오름차순으로 댓글을 조회할 수 있다")
    void findAllByCursor_orderByAsc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);
        createAndSaveComment("댓글 3", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments)
                .extracting(PostComment::getContent)
                .containsExactly("댓글 1", "댓글 2", "댓글 3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - limit을 적용하여 댓글을 조회할 수 있다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);
        createAndSaveComment("댓글 3", post, user);
        createAndSaveComment("댓글 4", post, user);
        createAndSaveComment("댓글 5", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments)
                .extracting(PostComment::getContent)
                .containsExactly("댓글 5", "댓글 4", "댓글 3");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 커서와 after를 사용하여 다음 페이지를 조회할 수 있다 (desc)")
    void findAllByCursor_withCursorAndAfter_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);
        createAndSaveComment("댓글 3", post, user);
        createAndSaveComment("댓글 4", post, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<PostComment> firstPage = postCommentRepository.findAllByCursor(
                post.getId(),
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
        PostComment lastComment = firstPage.getLast();
        List<PostComment> secondPage = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(PostComment::getContent)
                .containsExactly("댓글 2", "댓글 1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 커서와 after를 사용하여 다음 페이지를 조회할 수 있다 (asc)")
    void findAllByCursor_withCursorAndAfter_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);
        createAndSaveComment("댓글 3", post, user);
        createAndSaveComment("댓글 4", post, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<PostComment> firstPage = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then - 첫 페이지 검증
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage)
                .extracting(PostComment::getContent)
                .containsExactly("댓글 1", "댓글 2");

        // when - 두 번째 페이지 조회
        PostComment lastComment = firstPage.getLast();
        List<PostComment> secondPage = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage)
                .extracting(PostComment::getContent)
                .containsExactly("댓글 3", "댓글 4");
    }

    @Test
    @DisplayName("키워드로 댓글을 검색할 수 있다")
    void findAllByCursor_withKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("정말 좋은 게시물이네요", post, user);
        createAndSaveComment("별로에요", post, user);
        createAndSaveComment("좋은 정보 감사합니다", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                "좋은",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments)
                .extracting(PostComment::getContent)
                .containsExactlyInAnyOrder("정말 좋은 게시물이네요", "좋은 정보 감사합니다");
    }

    @Test
    @DisplayName("존재하지 않는 게시물 ID로 조회하면 빈 리스트를 반환한다")
    void findAllByCursor_nonExistentPostId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                999L,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("키워드가 일치하는 댓글이 없으면 빈 리스트를 반환한다")
    void findAllByCursor_noMatchingKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                "존재하지않는키워드",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("여러 사용자가 작성한 댓글을 조회할 수 있다")
    void findAll_byMultipleUsers() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");
        Post post = createPost("게시물 내용", user1);

        createAndSaveComment("user1의 댓글", post, user1);
        createAndSaveComment("user2의 댓글", post, user2);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments)
                .extracting(PostComment::getContent)
                .containsExactlyInAnyOrder("user1의 댓글", "user2의 댓글");
    }

    @Test
    @DisplayName("댓글이 없는 게시물의 댓글 개수는 0이다")
    void countByPost_emptyPost() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        em.flush();
        em.clear();

        // when
        long count = postCommentRepository.countByPost(post);

        // then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("댓글이 없는 게시물들의 댓글 개수를 조회하면 빈 리스트를 반환한다")
    void countCommentsByPostIds_emptyPosts() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post1 = createPost("게시물 1", user);
        Post post2 = createPost("게시물 2", user);

        em.flush();
        em.clear();

        // when
        List<PostCommentRepository.PostCommentCountProjection> result =
                postCommentRepository.countCommentsByPostIds(
                        List.of(post1.getId(), post2.getId())
                );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getContent()).isEqualTo("댓글 1");
        assertThat(comments.get(1).getContent()).isEqualTo("댓글 2");
    }

    @Test
    @DisplayName("postId가 null인 경우 모든 게시물의 댓글을 조회한다")
    void findAllByCursor_withNullPostId() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post1 = createPost("게시물 1", user);
        Post post2 = createPost("게시물 2", user);

        createAndSaveComment("게시물1 댓글", post1, user);
        createAndSaveComment("게시물2 댓글", post2, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
    }

    @Test
    @DisplayName("키워드가 빈 문자열인 경우 모든 댓글을 조회한다")
    void findAllByCursor_withEmptyKeyword() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                "",
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
    }

    @Test
    @DisplayName("키워드 검색 시 대소문자를 구분하지 않는다")
    void findAllByCursor_withKeywordCaseInsensitive() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("GOOD 좋은 댓글", post, user);
        createAndSaveComment("good 또 다른 댓글", post, user);
        createAndSaveComment("나쁜 댓글", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                "good",
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(comments).hasSize(2);
    }

    @Test
    @DisplayName("커서만 있고 after가 null인 경우에도 정상적으로 조회한다")
    void findAllByCursor_withCursorOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);
        createAndSaveComment("댓글 3", post, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<PostComment> firstPage = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        PostComment lastComment = firstPage.getFirst();

        // when - cursor만 있고 after는 null
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                lastComment.getCreatedAt().toString(),
                null,
                10
        );

        // then - cursor만으로도 조회 가능
        assertThat(comments).isNotEmpty();
    }

    @Test
    @DisplayName("after만 있고 cursor가 null인 경우에도 정상적으로 조회한다")
    void findAllByCursor_withAfterOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        PostComment comment1 = createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);
        createAndSaveComment("댓글 3", post, user);

        em.flush();
        em.clear();

        // when - after만 있고 cursor는 null
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                null,
                comment1.getId(),
                10
        );

        // then - after만으로도 조회 가능
        assertThat(comments).isNotEmpty();
    }

    @Test
    @DisplayName("limit이 1인 경우 1개의 댓글만 조회한다")
    void findAllByCursor_withLimitOne() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);
        createAndSaveComment("댓글 3", post, user);

        em.flush();
        em.clear();

        // when
        List<PostComment> comments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                1
        );

        // then
        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().getContent()).isEqualTo("댓글 3");
    }

    @Test
    @DisplayName("여러 페이지에 걸친 댓글 목록을 순차적으로 조회할 수 있다 (desc)")
    void findAllByCursor_multiplePages_desc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        for (int i = 1; i <= 10; i++) {
            createAndSaveComment("댓글 " + i, post, user);
        }

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<PostComment> page1 = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then - 첫 번째 페이지 검증
        assertThat(page1).hasSize(3);

        // when - 두 번째 페이지
        PostComment lastOfPage1 = page1.getLast();
        List<PostComment> page2 = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                lastOfPage1.getCreatedAt().toString(),
                lastOfPage1.getId(),
                3
        );

        // then - 두 번째 페이지 검증
        assertThat(page2).hasSize(3);

        // when - 세 번째 페이지
        PostComment lastOfPage2 = page2.getLast();
        List<PostComment> page3 = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                lastOfPage2.getCreatedAt().toString(),
                lastOfPage2.getId(),
                3
        );

        // then - 세 번째 페이지 검증
        assertThat(page3).hasSize(3);

        // when - 네 번째 페이지
        PostComment lastOfPage3 = page3.getLast();
        List<PostComment> page4 = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                lastOfPage3.getCreatedAt().toString(),
                lastOfPage3.getId(),
                3
        );

        // then - 네 번째 페이지 검증 (마지막 1개만)
        assertThat(page4).hasSize(1);
    }

    @Test
    @DisplayName("여러 페이지에 걸친 댓글 목록을 순차적으로 조회할 수 있다 (asc)")
    void findAllByCursor_multiplePages_asc() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        for (int i = 1; i <= 10; i++) {
            createAndSaveComment("댓글 " + i, post, user);
        }

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<PostComment> page1 = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                null,
                null,
                3
        );

        // then - 첫 번째 페이지 검증
        assertThat(page1).hasSize(3);

        // when - 두 번째 페이지
        PostComment lastOfPage1 = page1.getLast();
        List<PostComment> page2 = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                lastOfPage1.getCreatedAt().toString(),
                lastOfPage1.getId(),
                3
        );

        // then - 두 번째 페이지 검증
        assertThat(page2).hasSize(3);

        // when - 세 번째 페이지
        PostComment lastOfPage2 = page2.getLast();
        List<PostComment> page3 = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                lastOfPage2.getCreatedAt().toString(),
                lastOfPage2.getId(),
                3
        );

        // then - 세 번째 페이지 검증
        assertThat(page3).hasSize(3);

        // when - 네 번째 페이지
        PostComment lastOfPage3 = page3.getLast();
        List<PostComment> page4 = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "asc",
                lastOfPage3.getCreatedAt().toString(),
                lastOfPage3.getId(),
                3
        );

        // then - 네 번째 페이지 검증 (마지막 1개만)
        assertThat(page4).hasSize(1);
    }

    @Test
    @DisplayName("키워드 검색과 커서 페이지네이션을 함께 사용할 수 있다")
    void findAllByCursor_withKeywordAndCursor() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("좋은 댓글 1", post, user);
        createAndSaveComment("나쁜 댓글", post, user);
        createAndSaveComment("좋은 댓글 2", post, user);
        createAndSaveComment("좋은 댓글 3", post, user);

        em.flush();
        em.clear();

        // when - 첫 번째 페이지
        List<PostComment> page1 = postCommentRepository.findAllByCursor(
                post.getId(),
                "좋은",
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then - 첫 번째 페이지 검증
        assertThat(page1).hasSize(2);

        // when - 두 번째 페이지
        PostComment lastOfPage1 = page1.getLast();
        List<PostComment> page2 = postCommentRepository.findAllByCursor(
                post.getId(),
                "좋은",
                "createdAt",
                "asc",
                lastOfPage1.getCreatedAt().toString(),
                lastOfPage1.getId(),
                2
        );

        // then - 두 번째 페이지 검증
        assertThat(page2).hasSize(1);
        assertThat(page2.getFirst().getContent()).isEqualTo("좋은 댓글 3");
    }

    @Test
    @DisplayName("마지막 페이지 이후 조회 시 빈 리스트를 반환한다")
    void findAllByCursor_afterLastPage() {
        // given
        User user = createUser("testUser", "test@email.com");
        Post post = createPost("게시물 내용", user);

        createAndSaveComment("댓글 1", post, user);
        createAndSaveComment("댓글 2", post, user);

        em.flush();
        em.clear();

        // when - 모든 댓글 조회
        List<PostComment> allComments = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        PostComment lastComment = allComments.getLast();

        // when - 마지막 댓글 이후 조회
        List<PostComment> emptyPage = postCommentRepository.findAllByCursor(
                post.getId(),
                null,
                "createdAt",
                "desc",
                lastComment.getCreatedAt().toString(),
                lastComment.getId(),
                10
        );

        // then
        assertThat(emptyPage).isEmpty();
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

    private Post createPost(String content, User user) {
        Post post = Post.createPost(
                content,
                new ArrayList<>(),
                user,
                new ArrayList<>()
        );
        return postRepository.save(post);
    }

    private PostComment createComment(String content, Post post, User user) {
        return PostComment.createPostComment(content, post, user);
    }

    private PostComment createAndSaveComment(String content, Post post, User user) {
        PostComment comment = createComment(content, post, user);
        return postCommentRepository.save(comment);
    }
}

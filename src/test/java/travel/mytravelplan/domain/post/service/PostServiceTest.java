package travel.mytravelplan.domain.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import travel.mytravelplan.domain.post.dto.*;
import travel.mytravelplan.domain.post.entity.*;
import travel.mytravelplan.domain.post.exception.PostException;
import travel.mytravelplan.domain.post.mapper.PostBookMarkMapper;
import travel.mytravelplan.domain.post.mapper.PostLikeMapper;
import travel.mytravelplan.domain.post.mapper.PostMapper;
import travel.mytravelplan.domain.post.repository.*;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@DisplayName("게시글 서비스 테스트")
class PostServiceTest extends ServiceTestSupport {

    @Mock
    private PostRepository postRepository;

    @Mock
    private HashTagRepository hashTagRepository;

    @Mock
    private PostHashTagRepository postHashTagRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostBookMarkRepository postBookMarkRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private PostBookMarkMapper postBookMarkMapper;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;
    private HashTag hashTag;

    @BeforeEach
    void setUp() {
        user = User.createUser(
                "testuser",
                "password123",
                "test@example.com",
                SocialType.GOOGLE,
                "social123",
                Set.of(Role.USER)
        );

        hashTag = HashTag.createHashTag("여행");
        post = Post.createPost("테스트 게시글", List.of("image1.jpg"), user, List.of(hashTag));

        // Post 객체에 ID와 createdAt 설정
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("게시글을 생성한다")
    void createPost() {
        // given
        PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
                .content("테스트 게시글")
                .imageUrls(List.of("image1.jpg"))
                .hashTags(List.of("여행"))
                .build();

        given(hashTagRepository.findByName(eq("여행"))).willReturn(Optional.of(hashTag));
        given(postRepository.save(any(Post.class))).willReturn(post);

        PostDto expectedDto = PostDto.builder()
                .id(1L)
                .content("테스트 게시글")
                .imageUrls(List.of("image1.jpg"))
                .hashTags(List.of("여행"))
                .build();
        given(postMapper.toDto(any(Post.class), eq(user))).willReturn(expectedDto);

        // when
        PostDto result = postService.createPost(user, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("테스트 게시글");
        assertThat(result.getImageUrls()).containsExactly("image1.jpg");
        assertThat(result.getHashTags()).containsExactly("여행");

        then(hashTagRepository).should().findByName(eq("여행"));
        then(postRepository).should().save(any(Post.class));
        then(postHashTagRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("게시글 생성 시 존재하지 않는 해시태그는 새로 생성한다")
    void createPostWithNewHashTag() {
        // given
        PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
                .content("테스트 게시글")
                .imageUrls(List.of("image1.jpg"))
                .hashTags(List.of("신규태그"))
                .build();

        HashTag newHashTag = HashTag.createHashTag("신규태그");
        given(hashTagRepository.findByName(eq("신규태그"))).willReturn(Optional.empty());
        given(hashTagRepository.save(any(HashTag.class))).willReturn(newHashTag);
        given(postRepository.save(any(Post.class))).willReturn(post);

        PostDto expectedDto = PostDto.builder()
                .id(1L)
                .content("테스트 게시글")
                .build();
        given(postMapper.toDto(any(Post.class), eq(user))).willReturn(expectedDto);

        // when
        PostDto result = postService.createPost(user, requestDto);

        // then
        assertThat(result).isNotNull();

        then(hashTagRepository).should().findByName(eq("신규태그"));
        then(hashTagRepository).should().save(any(HashTag.class));
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("해시태그가 존재하면 조회만 하고 저장하지 않는다 (orElseGet 미실행)")
    void createPostWithExistingHashTag_orElseGetNotCalled() {
        // given
        PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
                .content("테스트 게시글")
                .imageUrls(List.of("image1.jpg"))
                .hashTags(List.of("여행"))
                .build();

        given(hashTagRepository.findByName(eq("여행"))).willReturn(Optional.of(hashTag));
        given(postRepository.save(any(Post.class))).willReturn(post);

        PostDto expectedDto = PostDto.builder()
                .id(1L)
                .content("테스트 게시글")
                .build();
        given(postMapper.toDto(any(Post.class), eq(user))).willReturn(expectedDto);

        // when
        PostDto result = postService.createPost(user, requestDto);

        // then
        assertThat(result).isNotNull();

        // findByName은 호출되지만 orElseGet 내부의 save는 호출되지 않아야 함
        then(hashTagRepository).should().findByName(eq("여행"));
        then(hashTagRepository).should(never()).save(any(HashTag.class));
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("해시태그가 존재하지 않으면 orElseGet으로 새로 생성하여 저장한다")
    void createPostWithNewHashTag_orElseGetCalled() {
        // given
        PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
                .content("테스트 게시글")
                .imageUrls(List.of("image1.jpg"))
                .hashTags(List.of("신규태그"))
                .build();

        HashTag newHashTag = HashTag.createHashTag("신규태그");
        given(hashTagRepository.findByName(eq("신규태그"))).willReturn(Optional.empty());
        given(hashTagRepository.save(any(HashTag.class))).willReturn(newHashTag);
        given(postRepository.save(any(Post.class))).willReturn(post);

        PostDto expectedDto = PostDto.builder()
                .id(1L)
                .content("테스트 게시글")
                .build();
        given(postMapper.toDto(any(Post.class), eq(user))).willReturn(expectedDto);

        // when
        PostDto result = postService.createPost(user, requestDto);

        // then
        assertThat(result).isNotNull();

        // findByName이 Optional.empty()를 반환하므로 orElseGet 내부의 save가 호출되어야 함
        then(hashTagRepository).should().findByName(eq("신규태그"));
        then(hashTagRepository).should().save(any(HashTag.class));
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("여러 해시태그 중 일부는 존재하고 일부는 신규인 경우 orElseGet이 선택적으로 실행된다")
    void createPostWithMixedHashTags_orElseGetPartiallyExecuted() {
        // given
        PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
                .content("테스트 게시글")
                .imageUrls(List.of("image1.jpg"))
                .hashTags(List.of("여행", "신규태그1", "신규태그2"))
                .build();

        HashTag newHashTag1 = HashTag.createHashTag("신규태그1");
        HashTag newHashTag2 = HashTag.createHashTag("신규태그2");

        given(hashTagRepository.findByName(eq("여행"))).willReturn(Optional.of(hashTag));
        given(hashTagRepository.findByName(eq("신규태그1"))).willReturn(Optional.empty());
        given(hashTagRepository.findByName(eq("신규태그2"))).willReturn(Optional.empty());
        given(hashTagRepository.save(any(HashTag.class)))
                .willReturn(newHashTag1)
                .willReturn(newHashTag2);
        given(postRepository.save(any(Post.class))).willReturn(post);

        PostDto expectedDto = PostDto.builder()
                .id(1L)
                .content("테스트 게시글")
                .build();
        given(postMapper.toDto(any(Post.class), eq(user))).willReturn(expectedDto);

        // when
        PostDto result = postService.createPost(user, requestDto);

        // then
        assertThat(result).isNotNull();

        // "여행"은 존재하므로 findByName만 호출, "신규태그1"과 "신규태그2"는 orElseGet으로 save 호출
        then(hashTagRepository).should().findByName(eq("여행"));
        then(hashTagRepository).should().findByName(eq("신규태그1"));
        then(hashTagRepository).should().findByName(eq("신규태그2"));
        then(hashTagRepository).should(times(2)).save(any(HashTag.class));
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 게시글 목록을 조회한다")
    void getPosts() {
        // given
        String keyword = "여행";
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Post> posts = List.of(post);
        given(postRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(posts);

        PostDto postDto = PostDto.builder()
                .id(1L)
                .content("테스트 게시글")
                .build();
        given(postMapper.toDto(anyList(), eq(user))).willReturn(List.of(postDto));

        // when
        CursorPageResponseDto<PostDto> result = postService.getPosts(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(1);

        then(postRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("다음 페이지가 있는 경우 hasNext가 true이고 nextCursor가 설정된다")
    void getPostsWithNextPage() {
        // given
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        User anotherUser = User.createUser(
                "another",
                "password123",
                "another@example.com",
                SocialType.GOOGLE,
                "social456",
                Set.of(Role.USER)
        );

        Post post1 = Post.createPost("첫 번째 게시글", List.of("image1.jpg"), user, List.of(hashTag));
        Post post2 = Post.createPost("두 번째 게시글", List.of("image2.jpg"), anotherUser, List.of(hashTag));

        ReflectionTestUtils.setField(post1, "id", 1L);
        ReflectionTestUtils.setField(post1, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(post2, "id", 2L);
        ReflectionTestUtils.setField(post2, "createdAt", LocalDateTime.now());

        List<Post> posts = List.of(post1, post2);
        given(postRepository.findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(posts);

        PostDto postDto = PostDto.builder().id(1L).build();
        given(postMapper.toDto(anyList(), eq(user))).willReturn(List.of(postDto));

        // when
        CursorPageResponseDto<PostDto> result = postService.getPosts(user, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(postRepository).should().findAllByCursor(eq("testuser"), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("특정 사용자의 게시글 목록을 조회한다")
    void getUserPosts() {
        // given
        String username = "targetuser";
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Post> posts = List.of(post);
        given(postRepository.findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(posts);

        PostDto postDto = PostDto.builder().id(1L).build();
        given(postMapper.toDto(anyList(), eq(user))).willReturn(List.of(postDto));

        // when
        CursorPageResponseDto<PostDto> result = postService.getUserPosts(user, username, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(postRepository).should().findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("특정 사용자의 게시글 목록 조회 시 다음 페이지가 있으면 hasNext가 true이다")
    void getUserPostsWithNextPage() {
        // given
        String username = "targetuser";
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 1;

        Post post1 = Post.createPost("첫 번째 게시글", List.of("image1.jpg"), user, List.of(hashTag));
        Post post2 = Post.createPost("두 번째 게시글", List.of("image2.jpg"), user, List.of(hashTag));

        ReflectionTestUtils.setField(post1, "id", 1L);
        ReflectionTestUtils.setField(post1, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(post2, "id", 2L);
        ReflectionTestUtils.setField(post2, "createdAt", LocalDateTime.now());

        List<Post> posts = List.of(post1, post2);
        given(postRepository.findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(posts);

        PostDto postDto = PostDto.builder().id(1L).build();
        given(postMapper.toDto(anyList(), eq(user))).willReturn(List.of(postDto));

        // when
        CursorPageResponseDto<PostDto> result = postService.getUserPosts(user, username, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        then(postRepository).should().findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("특정 사용자의 게시글을 키워드로 필터링하여 조회한다")
    void getUserPostsWithKeyword() {
        // given
        String username = "targetuser";
        String keyword = "여행";
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Post> posts = List.of(post);
        given(postRepository.findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(posts);

        PostDto postDto = PostDto.builder()
                .id(1L)
                .content("여행 관련 게시글")
                .build();
        given(postMapper.toDto(anyList(), eq(user))).willReturn(List.of(postDto));

        // when
        CursorPageResponseDto<PostDto> result = postService.getUserPosts(user, username, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(postRepository).should().findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("특정 사용자의 게시글이 없는 경우 빈 목록을 반환한다")
    void getUserPostsEmpty() {
        // given
        String username = "targetuser";
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = null;
        Long after = null;
        int limit = 10;

        List<Post> posts = List.of();
        given(postRepository.findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(posts);

        given(postMapper.toDto(anyList(), eq(user))).willReturn(List.of());

        // when
        CursorPageResponseDto<PostDto> result = postService.getUserPosts(user, username, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(0);
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();

        then(postRepository).should().findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("특정 사용자의 게시글을 커서 기반으로 다음 페이지를 조회한다")
    void getUserPostsWithCursor() {
        // given
        String username = "targetuser";
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "desc";
        String cursor = "2024-12-01T10:00:00";
        Long after = 5L;
        int limit = 10;

        List<Post> posts = List.of(post);
        given(postRepository.findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(posts);

        PostDto postDto = PostDto.builder().id(1L).build();
        given(postMapper.toDto(anyList(), eq(user))).willReturn(List.of(postDto));

        // when
        CursorPageResponseDto<PostDto> result = postService.getUserPosts(user, username, keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isFalse();

        then(postRepository).should().findAllByCursor(eq(username), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
    }

    @Test
    @DisplayName("게시글 ID로 단건 조회한다")
    void getPost() {
        // given
        Long postId = 1L;
        given(postRepository.findWithUserById(eq(postId))).willReturn(Optional.of(post));

        PostDto expectedDto = PostDto.builder()
                .id(postId)
                .content("테스트 게시글")
                .build();
        given(postMapper.toDto(eq(post), eq(user))).willReturn(expectedDto);

        // when
        PostDto result = postService.getPost(user, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
        assertThat(result.getContent()).isEqualTo("테스트 게시글");

        then(postRepository).should().findWithUserById(eq(postId));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
    void getPostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findWithUserById(eq(postId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(user, postId))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findWithUserById(eq(postId));
    }

    @Test
    @DisplayName("게시글을 수정한다")
    void updatePost() {
        // given
        Long postId = 1L;
        PostUpdateRequestDto requestDto = PostUpdateRequestDto.builder()
                .content("수정된 게시글")
                .imageUrls(List.of("new-image.jpg"))
                .hashTags(List.of("수정태그"))
                .build();

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));

        HashTag newHashTag = HashTag.createHashTag("수정태그");
        given(hashTagRepository.findByName(eq("수정태그"))).willReturn(Optional.of(newHashTag));

        PostDto expectedDto = PostDto.builder()
                .id(postId)
                .content("수정된 게시글")
                .imageUrls(List.of("new-image.jpg"))
                .hashTags(List.of("수정태그"))
                .build();
        given(postMapper.toDto(eq(post), eq(user))).willReturn(expectedDto);

        // when
        PostDto result = postService.updatePost(user, postId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("수정된 게시글");
        assertThat(result.getImageUrls()).containsExactly("new-image.jpg");
        assertThat(result.getHashTags()).containsExactly("수정태그");

        then(postRepository).should().findById(eq(postId));
        then(hashTagRepository).should().findByName(eq("수정태그"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 예외가 발생한다")
    void updatePostNotFound() {
        // given
        Long postId = 999L;
        PostUpdateRequestDto requestDto = PostUpdateRequestDto.builder()
                .content("수정된 게시글")
                .imageUrls(new ArrayList<>())
                .hashTags(new ArrayList<>())
                .build();

        given(postRepository.findById(eq(postId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(user, postId, requestDto))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(postId));
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void deletePost() {
        // given
        Long postId = 1L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));

        // when
        postService.deletePost(postId);

        // then
        then(postRepository).should().findById(eq(postId));
        then(postRepository).should().delete(eq(post));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외가 발생한다")
    void deletePostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.deletePost(postId))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(postId));
    }

    @Test
    @DisplayName("게시글에 좋아요를 누른다")
    void likePost() {
        // given
        Long postId = 1L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostAndUser(eq(post), eq(user))).willReturn(Optional.empty());

        PostLike postLike = PostLike.createPostLike(post, user);
        given(postLikeRepository.save(any(PostLike.class))).willReturn(postLike);

        PostLikeDto expectedDto = PostLikeDto.builder()
                .postId(postId)
                .userId(1L)
                .liked(true)
                .build();
        given(postLikeMapper.toDto(any(PostLike.class), eq(true))).willReturn(expectedDto);

        // when
        PostLikeDto result = postService.likePost(user, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isLiked()).isTrue();
        assertThat(result.getPostId()).isEqualTo(postId);

        then(postRepository).should().findById(eq(postId));
        then(postLikeRepository).should().findByPostAndUser(eq(post), eq(user));
        then(postLikeRepository).should().save(any(PostLike.class));
    }

    @Test
    @DisplayName("이미 좋아요한 게시글에 다시 좋아요를 누르면 취소된다")
    void unlikePost() {
        // given
        Long postId = 1L;
        PostLike postLike = PostLike.createPostLike(post, user);

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostAndUser(eq(post), eq(user))).willReturn(Optional.of(postLike));

        PostLikeDto expectedDto = PostLikeDto.builder()
                .postId(postId)
                .userId(1L)
                .liked(false)
                .build();
        given(postLikeMapper.toDto(eq(postLike), eq(false))).willReturn(expectedDto);

        // when
        PostLikeDto result = postService.likePost(user, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isLiked()).isFalse();
        assertThat(result.getPostId()).isEqualTo(postId);

        then(postRepository).should().findById(eq(postId));
        then(postLikeRepository).should().findByPostAndUser(eq(post), eq(user));
        then(postLikeRepository).should().delete(eq(postLike));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요 시 예외가 발생한다")
    void likePostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.likePost(user, postId))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(postId));
    }

    @Test
    @DisplayName("게시글을 북마크한다")
    void bookmarkPost() {
        // given
        Long postId = 1L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postBookMarkRepository.findByPostAndUser(eq(post), eq(user))).willReturn(Optional.empty());

        PostBookMark postBookMark = PostBookMark.createPostBookMark(post, user);
        given(postBookMarkRepository.save(any(PostBookMark.class))).willReturn(postBookMark);

        PostBookMarkDto expectedDto = PostBookMarkDto.builder()
                .postId(postId)
                .userId(1L)
                .bookmarked(true)
                .build();
        given(postBookMarkMapper.toDto(any(PostBookMark.class), eq(true))).willReturn(expectedDto);

        // when
        PostBookMarkDto result = postService.bookmarkPost(user, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isBookmarked()).isTrue();
        assertThat(result.getPostId()).isEqualTo(postId);

        then(postRepository).should().findById(eq(postId));
        then(postBookMarkRepository).should().findByPostAndUser(eq(post), eq(user));
        then(postBookMarkRepository).should().save(any(PostBookMark.class));
    }

    @Test
    @DisplayName("이미 북마크한 게시글에 다시 북마크를 누르면 취소된다")
    void unbookmarkPost() {
        // given
        Long postId = 1L;
        PostBookMark postBookMark = PostBookMark.createPostBookMark(post, user);

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postBookMarkRepository.findByPostAndUser(eq(post), eq(user))).willReturn(Optional.of(postBookMark));

        PostBookMarkDto expectedDto = PostBookMarkDto.builder()
                .postId(postId)
                .userId(1L)
                .bookmarked(false)
                .build();
        given(postBookMarkMapper.toDto(eq(postBookMark), eq(false))).willReturn(expectedDto);

        // when
        PostBookMarkDto result = postService.bookmarkPost(user, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isBookmarked()).isFalse();
        assertThat(result.getPostId()).isEqualTo(postId);

        then(postRepository).should().findById(eq(postId));
        then(postBookMarkRepository).should().findByPostAndUser(eq(post), eq(user));
        then(postBookMarkRepository).should().delete(eq(postBookMark));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 북마크 시 예외가 발생한다")
    void bookmarkPostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.bookmarkPost(user, postId))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(postId));
    }
}
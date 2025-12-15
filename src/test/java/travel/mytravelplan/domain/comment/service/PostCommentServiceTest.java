package travel.mytravelplan.domain.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import travel.mytravelplan.domain.comment.dto.PostCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.PostCommentDto;
import travel.mytravelplan.domain.comment.dto.PostCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.entity.PostComment;
import travel.mytravelplan.domain.comment.exception.PostCommentException;
import travel.mytravelplan.domain.comment.mapper.PostCommentMapper;
import travel.mytravelplan.domain.comment.repository.PostCommentRepository;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.exception.PostException;
import travel.mytravelplan.domain.post.repository.PostRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.support.ServiceTestSupport;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("게시글 댓글 서비스 테스트")
class PostCommentServiceTest extends ServiceTestSupport {

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentMapper postCommentMapper;

    @InjectMocks
    private PostCommentService postCommentService;

    private User user;
    private Post post;
    private PostComment postComment;
    private PostCommentDto postCommentDto;
    private PostCommentCreateRequestDto createRequestDto;
    private PostCommentUpdateRequestDto updateRequestDto;

    private Long postId;
    private Long postCommentId;

    @BeforeEach
    void setUp() {
        // 공통 ID 설정
        postId = 1L;
        postCommentId = 1L;

        // User 설정
        user = User.createUser("testuser", "password", "test@test.com", null, null, null);

        // Post 설정 및 리플렉션으로 ID 주입
        post = Post.createPost("테스트 게시글", List.of(), user, List.of());
        ReflectionTestUtils.setField(post, "id", postId);

        // PostComment 설정 및 리플렉션으로 ID 주입
        postComment = PostComment.createPostComment("테스트 댓글", post, user);
        ReflectionTestUtils.setField(postComment, "id", postCommentId);
        ReflectionTestUtils.setField(postComment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(postComment, "updatedAt", LocalDateTime.now());

        // PostCommentDto 설정
        postCommentDto = PostCommentDto.builder()
                .id(postCommentId)
                .postId(postId)
                .userId(1L)
                .username("testuser")
                .content("테스트 댓글")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 생성 요청 DTO 설정
        createRequestDto = PostCommentCreateRequestDto.builder()
                .content("새로운 댓글")
                .build();

        // 수정 요청 DTO 설정
        updateRequestDto = PostCommentUpdateRequestDto.builder()
                .content("수정된 댓글")
                .build();
    }

    @Test
    @DisplayName("게시글 댓글 생성 성공")
    void createPostComment_Success() {
        // given
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.save(any(PostComment.class))).willReturn(postComment);
        given(postCommentMapper.toDto(any(PostComment.class))).willReturn(postCommentDto);

        // when
        PostCommentDto result = postCommentService.createPostComment(user, postId, createRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(postCommentDto);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().save(any(PostComment.class));
        then(postCommentMapper).should().toDto(any(PostComment.class));
    }

    @Test
    @DisplayName("게시글 댓글 생성 실패 - 게시글을 찾을 수 없음")
    void createPostComment_PostNotFound() {
        // given
        Long invalidPostId = 999L;
        given(postRepository.findById(eq(invalidPostId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommentService.createPostComment(user, invalidPostId, createRequestDto))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(invalidPostId));
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회 성공")
    void getPostComments_Success() {
        // given
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 10;

        PostComment postComment2 = PostComment.createPostComment("테스트 댓글2", post, user);
        ReflectionTestUtils.setField(postComment2, "id", 2L);
        ReflectionTestUtils.setField(postComment2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(postComment2, "updatedAt", LocalDateTime.now());
        List<PostComment> postComments = Arrays.asList(postComment, postComment2);

        PostCommentDto postCommentDto2 = PostCommentDto.builder()
                .id(2L)
                .postId(postId)
                .userId(1L)
                .username("testuser")
                .content("테스트 댓글2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findAllByCursor(eq(postId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(postComments);
        given(postCommentMapper.toDto(eq(postComment))).willReturn(postCommentDto);
        given(postCommentMapper.toDto(eq(postComment2))).willReturn(postCommentDto2);

        // when
        CursorPageResponseDto<PostCommentDto> result = postCommentService.getPostComments(
                postId, keyword, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(postCommentDto, postCommentDto2);
        assertThat(result.getHasNext()).isFalse();

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findAllByCursor(eq(postId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(postCommentMapper).should().toDto(eq(postComment));
        then(postCommentMapper).should().toDto(eq(postComment2));
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회 성공 - 다음 페이지 있음")
    void getPostComments_WithNextPage_Success() {
        // given
        String keyword = null;
        String orderBy = "createdAt";
        String direction = "DESC";
        String cursor = null;
        Long after = null;
        int limit = 1;

        PostComment postComment2 = PostComment.createPostComment("테스트 댓글2", post, user);
        ReflectionTestUtils.setField(postComment2, "id", 2L);
        ReflectionTestUtils.setField(postComment2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(postComment2, "updatedAt", LocalDateTime.now());
        List<PostComment> postComments = Arrays.asList(postComment, postComment2);

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findAllByCursor(eq(postId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
                .willReturn(postComments);
        given(postCommentMapper.toDto(eq(postComment))).willReturn(postCommentDto);

        // when
        CursorPageResponseDto<PostCommentDto> result = postCommentService.getPostComments(
                postId, keyword, orderBy, direction, cursor, after, limit
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextAfter()).isNotNull();

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findAllByCursor(eq(postId), eq(keyword), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit + 1));
        then(postCommentMapper).should().toDto(eq(postComment));
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회 실패 - 게시글을 찾을 수 없음")
    void getPostComments_PostNotFound() {
        // given
        Long invalidPostId = 999L;
        given(postRepository.findById(eq(invalidPostId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommentService.getPostComments(
                invalidPostId, null, "createdAt", "DESC", null, null, 10
        )).isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(invalidPostId));
    }

    @Test
    @DisplayName("게시글 댓글 단건 조회 성공")
    void getPostComment_Success() {
        // given
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(postCommentId))).willReturn(Optional.of(postComment));
        given(postCommentMapper.toDto(eq(postComment))).willReturn(postCommentDto);

        // when
        PostCommentDto result = postCommentService.getPostComment(postId, postCommentId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(postCommentDto);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(postCommentId));
        then(postCommentMapper).should().toDto(eq(postComment));
    }

    @Test
    @DisplayName("게시글 댓글 단건 조회 실패 - 게시글을 찾을 수 없음")
    void getPostComment_PostNotFound() {
        // given
        Long invalidPostId = 999L;
        given(postRepository.findById(eq(invalidPostId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommentService.getPostComment(invalidPostId, postCommentId))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(invalidPostId));
    }

    @Test
    @DisplayName("게시글 댓글 단건 조회 실패 - 댓글을 찾을 수 없음")
    void getPostComment_CommentNotFound() {
        // given
        Long invalidPostCommentId = 999L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(invalidPostCommentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommentService.getPostComment(postId, invalidPostCommentId))
                .isInstanceOf(PostCommentException.class);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(invalidPostCommentId));
    }

    @Test
    @DisplayName("게시글 댓글 단건 조회 실패 - 댓글이 게시글에 속하지 않음")
    void getPostComment_CommentNotBelongToPost() {
        // given
        Post anotherPost = Post.createPost("다른 게시글", List.of(), user, List.of());
        PostComment anotherPostComment = PostComment.createPostComment("다른 댓글", anotherPost, user);

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(postCommentId))).willReturn(Optional.of(anotherPostComment));

        // when & then
        assertThatThrownBy(() -> postCommentService.getPostComment(postId, postCommentId))
                .isInstanceOf(PostCommentException.class);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(postCommentId));
    }

    @Test
    @DisplayName("게시글 댓글 수정 성공")
    void updatePostComment_Success() {
        // given
        PostCommentDto updatedDto = PostCommentDto.builder()
                .id(postCommentId)
                .postId(postId)
                .userId(1L)
                .username("testuser")
                .content("수정된 댓글")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(postCommentId))).willReturn(Optional.of(postComment));
        given(postCommentMapper.toDto(eq(postComment))).willReturn(updatedDto);

        // when
        PostCommentDto result = postCommentService.updatePostComment(postId, postCommentId, updateRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(updatedDto);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(postCommentId));
        then(postCommentMapper).should().toDto(eq(postComment));
    }

    @Test
    @DisplayName("게시글 댓글 수정 실패 - 게시글을 찾을 수 없음")
    void updatePostComment_PostNotFound() {
        // given
        Long invalidPostId = 999L;
        given(postRepository.findById(eq(invalidPostId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommentService.updatePostComment(invalidPostId, postCommentId, updateRequestDto))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(invalidPostId));
    }

    @Test
    @DisplayName("게시글 댓글 수정 실패 - 댓글을 찾을 수 없음")
    void updatePostComment_CommentNotFound() {
        // given
        Long invalidPostCommentId = 999L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(invalidPostCommentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommentService.updatePostComment(postId, invalidPostCommentId, updateRequestDto))
                .isInstanceOf(PostCommentException.class);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(invalidPostCommentId));
    }

    @Test
    @DisplayName("게시글 댓글 수정 실패 - 댓글이 게시글에 속하지 않음")
    void updatePostComment_CommentNotBelongToPost() {
        // given
        Post anotherPost = Post.createPost("다른 게시글", List.of(), user, List.of());
        PostComment anotherPostComment = PostComment.createPostComment("다른 댓글", anotherPost, user);

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(postCommentId))).willReturn(Optional.of(anotherPostComment));

        // when & then
        assertThatThrownBy(() -> postCommentService.updatePostComment(postId, postCommentId, updateRequestDto))
                .isInstanceOf(PostCommentException.class);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(postCommentId));
    }

    @Test
    @DisplayName("게시글 댓글 삭제 성공")
    void deletePostComment_Success() {
        // given
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(postCommentId))).willReturn(Optional.of(postComment));

        // when
        postCommentService.deletePostComment(postId, postCommentId);

        // then
        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(postCommentId));
        then(postCommentRepository).should().delete(eq(postComment));
    }

    @Test
    @DisplayName("게시글 댓글 삭제 실패 - 게시글을 찾을 수 없음")
    void deletePostComment_PostNotFound() {
        // given
        Long invalidPostId = 999L;
        given(postRepository.findById(eq(invalidPostId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommentService.deletePostComment(invalidPostId, postCommentId))
                .isInstanceOf(PostException.class);

        then(postRepository).should().findById(eq(invalidPostId));
    }

    @Test
    @DisplayName("게시글 댓글 삭제 실패 - 댓글을 찾을 수 없음")
    void deletePostComment_CommentNotFound() {
        // given
        Long invalidPostCommentId = 999L;
        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(invalidPostCommentId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommentService.deletePostComment(postId, invalidPostCommentId))
                .isInstanceOf(PostCommentException.class);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(invalidPostCommentId));
    }

    @Test
    @DisplayName("게시글 댓글 삭제 실패 - 댓글이 게시글에 속하지 않음")
    void deletePostComment_CommentNotBelongToPost() {
        // given
        Post anotherPost = Post.createPost("다른 게시글", List.of(), user, List.of());
        PostComment anotherPostComment = PostComment.createPostComment("다른 댓글", anotherPost, user);

        given(postRepository.findById(eq(postId))).willReturn(Optional.of(post));
        given(postCommentRepository.findById(eq(postCommentId))).willReturn(Optional.of(anotherPostComment));

        // when & then
        assertThatThrownBy(() -> postCommentService.deletePostComment(postId, postCommentId))
                .isInstanceOf(PostCommentException.class);

        then(postRepository).should().findById(eq(postId));
        then(postCommentRepository).should().findById(eq(postCommentId));
    }
}
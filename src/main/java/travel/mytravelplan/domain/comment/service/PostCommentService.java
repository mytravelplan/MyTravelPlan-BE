package travel.mytravelplan.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import travel.mytravelplan.global.error.code.PostCommentErrorCode;
import travel.mytravelplan.global.error.code.PostErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final PostCommentMapper postCommentMapper;

    @Transactional
    public PostCommentDto createPostComment(User currentUser, Long postId, PostCommentCreateRequestDto postCommentCreateRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        PostComment postComment = PostComment.createPostComment(postCommentCreateRequestDto.getContent(), post, currentUser);

        postCommentRepository.save(postComment);

        return postCommentMapper.toDto(postComment);
    }

    public CursorPageResponseDto<PostCommentDto> getPostComments(Long postId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        List<PostComment> postComments = postCommentRepository.findAllByCursor(post.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = postComments.size() > limit;

        List<PostComment> pagedPostComments = hasNext ? postComments.subList(0, limit) : postComments;

        List<PostCommentDto> postCommentDtos = pagedPostComments.stream()
                .map(postCommentMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            PostComment lastPostComment = pagedPostComments.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastPostComment.getCreatedAt().toString();
            }

            nextAfter = lastPostComment.getId();
        }

        return CursorPageResponseDto.<PostCommentDto>builder()
                .content(postCommentDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(postCommentDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public PostCommentDto getPostComment(Long postId, Long postCommentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        PostComment postComment = postCommentRepository.findById(postCommentId)
                .orElseThrow(() -> new PostCommentException(PostCommentErrorCode.POST_COMMENT_NOT_FOUND));

        validatePostCommentBelongsToPost(postComment, post);

        return postCommentMapper.toDto(postComment);
    }

    @Transactional
    public PostCommentDto updatePostComment(Long postId, Long postCommentId, PostCommentUpdateRequestDto postCommentUpdateRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        PostComment postComment = postCommentRepository.findById(postCommentId)
                .orElseThrow(() -> new PostCommentException(PostCommentErrorCode.POST_COMMENT_NOT_FOUND));

        validatePostCommentBelongsToPost(postComment, post);

        postComment.update(postCommentUpdateRequestDto.getContent());

        return postCommentMapper.toDto(postComment);
    }

    @Transactional
    public void deletePostComment(Long postId, Long postCommentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        PostComment postComment = postCommentRepository.findById(postCommentId)
                .orElseThrow(() -> new PostCommentException(PostCommentErrorCode.POST_COMMENT_NOT_FOUND));

        validatePostCommentBelongsToPost(postComment, post);

        postCommentRepository.delete(postComment);
    }

    private void validatePostCommentBelongsToPost(PostComment postComment, Post post) {
        if (!postComment.getPost().equals(post)) {
            throw new PostCommentException(PostCommentErrorCode.POST_COMMENT_NOT_BELONG_TO_POST);
        }
    }
}

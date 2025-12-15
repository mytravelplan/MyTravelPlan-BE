package travel.mytravelplan.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.post.dto.*;
import travel.mytravelplan.domain.post.entity.*;
import travel.mytravelplan.domain.post.exception.PostException;
import travel.mytravelplan.domain.post.mapper.PostBookMarkMapper;
import travel.mytravelplan.domain.post.mapper.PostLikeMapper;
import travel.mytravelplan.domain.post.mapper.PostMapper;
import travel.mytravelplan.domain.post.repository.*;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.PostErrorCode;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final HashTagRepository hashTagRepository;
    private final PostHashTagRepository postHashTagRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookMarkRepository postBookMarkRepository;
    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostBookMarkMapper postBookMarkMapper;

    @Transactional
    public PostDto createPost(User user, PostCreateRequestDto postCreateRequestDto) {
        List<HashTag> hashTags = postCreateRequestDto.getHashTags().stream()
                .map(tagName -> hashTagRepository.findByName(tagName)
                        .orElseGet(() -> hashTagRepository.save(HashTag.createHashTag(tagName))))
                .toList();

        Post post = Post.createPost(postCreateRequestDto.getContent(), postCreateRequestDto.getImageUrls(), user, hashTags);
        postRepository.save(post);
        postHashTagRepository.saveAll(post.getPostHashTags());
        return postMapper.toDto(post, user);
    }

    public CursorPageResponseDto<PostDto> getPosts(User currentUser, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Post> posts =  postRepository.findAllByCursor(currentUser.getUsername(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = posts.size() > limit;

        List<Post> pagedPosts = hasNext ? posts.subList(0, limit) : posts;

        List<PostDto> postDtos = postMapper.toDto(pagedPosts, currentUser);

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Post lastPost = pagedPosts.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastPost.getCreatedAt().toString();
            }

            nextAfter = lastPost.getId();
        }

        return CursorPageResponseDto.<PostDto>builder()
                .content(postDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(postDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public CursorPageResponseDto<PostDto> getUserPosts(User currentUser, String username, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Post> posts = postRepository.findAllByCursor(username, keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = posts.size() > limit;
        List<Post> pagedPosts = hasNext ? posts.subList(0, limit) : posts;

        List<PostDto> postDtos = postMapper.toDto(pagedPosts, currentUser);

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Post lastPost = pagedPosts.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastPost.getCreatedAt().toString();
            }

            nextAfter = lastPost.getId();
        }

        return CursorPageResponseDto.<PostDto>builder()
                .content(postDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(postDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public PostDto getPost(User currentUser, Long postId) {
        Post post = postRepository.findWithUserById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        return postMapper.toDto(post, currentUser);
    }

    @Transactional
    public PostDto updatePost(User currentUser, Long postId, PostUpdateRequestDto postUpdateRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        List<HashTag> hashTags = postUpdateRequestDto.getHashTags().stream()
                .map(tagName -> hashTagRepository.findByName(tagName)
                        .orElseGet(() -> hashTagRepository.save(HashTag.createHashTag(tagName))))
                .toList();

        post.update(
                postUpdateRequestDto.getContent(),
                postUpdateRequestDto.getImageUrls(),
                hashTags
        );

        return postMapper.toDto(post, currentUser);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        postRepository.delete(post);
    }

    @Transactional
    public PostLikeDto likePost(User currentUser, Long postId)
    {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        Optional<PostLike> postLikeOptional = postLikeRepository.findByPostAndUser(post, currentUser);

        PostLike postLike;
        boolean isLiked;

        if (postLikeOptional.isPresent()) {
            postLike = postLikeOptional.get();
            postLikeRepository.delete(postLike);
            isLiked = false;
        } else {
            PostLike newPostLike = PostLike.createPostLike(post, currentUser);
            postLike = postLikeRepository.save(newPostLike);
            isLiked = true;
        }

        return postLikeMapper.toDto(postLike, isLiked);
    }

    @Transactional
    public PostBookMarkDto bookmarkPost(User currentUser, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        Optional<PostBookMark> postBookMarkOptional = postBookMarkRepository.findByPostAndUser(post, currentUser);

        PostBookMark postBookMark;
        boolean isBookMarked;

        if (postBookMarkOptional.isPresent()) {
            postBookMark = postBookMarkOptional.get();
            postBookMarkRepository.delete(postBookMark);
            isBookMarked = false;
        } else {
            postBookMark = PostBookMark.createPostBookMark(post, currentUser);
            postBookMarkRepository.save(postBookMark);
            isBookMarked = true;
        }

        return postBookMarkMapper.toDto(postBookMark, isBookMarked);
    }
}

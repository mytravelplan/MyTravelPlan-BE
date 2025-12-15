package travel.mytravelplan.domain.post.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.comment.repository.PostCommentRepository;
import travel.mytravelplan.domain.post.dto.PostDto;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.post.repository.PostBookMarkRepository;
import travel.mytravelplan.domain.post.repository.PostLikeRepository;
import travel.mytravelplan.domain.user.entity.User;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {Collectors.class, List.class, Map.class, HashMap.class, HashSet.class, Collections.class})
public abstract class PostMapper {

    @Autowired
    protected PostLikeRepository postLikeRepository;

    @Autowired
    protected PostBookMarkRepository postBookMarkRepository;

    @Autowired
    protected PostCommentRepository postCommentRepository;

    public PostDto toDto(Post post, User currentUser) {
        if (post == null) {
            return null;
        }

        List<Post> posts = Collections.singletonList(post);

        Map<Long, Long> likeCountMap = getLikeCountMap(posts);
        Map<Long, Long> commentCountMap = getCommentCountMap(posts);
        Map<Long, Boolean> likedMap = getLikedByMeMapByFeedIds(posts, currentUser);
        Map<Long, Boolean> bookmarkedMap = getBookmarkedByMeMapByFeedIds(posts, currentUser);

        return toDto(post,
                resolveHashTags(post),
                likeCountMap.getOrDefault(post.getId(), 0L).intValue(),
                commentCountMap.getOrDefault(post.getId(), 0L).intValue(),
                likedMap.getOrDefault(post.getId(), false),
                bookmarkedMap.getOrDefault(post.getId(), false)
        );
    }

/*
    public PostDto toDto(Post post, User currentUser) {
        if (post == null) {
            return null;
        }

        List<String> hashTags = resolveHashTags(post);
        long numberOfLikes = postLikeRepository.countByPost(post);
        long numberOfComments = postCommentRepository.countByPost(post);;
        boolean liked = postLikeRepository.existsByPostAndUser(post, currentUser);
        boolean bookmarked = postBookMarkRepository.existsByPostAndUser(post, currentUser);
        return toDto(post, hashTags, numberOfLikes, numberOfComments, liked, bookmarked);
    }
*/

    public List<PostDto> toDto(List<Post> posts, User currentUser) {
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> likeCountMap = getLikeCountMap(posts);
        Map<Long, Long> commentCountMap = getCommentCountMap(posts);
        Map<Long, Boolean> likedMap = getLikedByMeMapByFeedIds(posts, currentUser);
        Map<Long, Boolean> bookmarkedMap = getBookmarkedByMeMapByFeedIds(posts, currentUser);

        return posts.stream()
                .map(post -> {
                    List<String> hahTags = resolveHashTags(post);
                    Long numberOfLikes = likeCountMap.getOrDefault(post.getId(), 0L);
                    Long numberOfComments = commentCountMap.getOrDefault(post.getId(), 0L);
                    Boolean liked = likedMap.getOrDefault(post.getId(), false);
                    Boolean bookmarked = bookmarkedMap.getOrDefault(post.getId(), false);
                    return toDto(post, hahTags, numberOfLikes.intValue(), numberOfComments.intValue(), liked, bookmarked);
                })
                .collect(Collectors.toList());
    }

/*
    public List<PostDto> toDto(List<Post> posts, User currentUser) {
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        return posts.stream()
                .map(post -> {
                    List<String> hashTags = resolveHashTags(post);
                    long numberOfLikes = postLikeRepository.countByPost(post);
                    long numberOfComments = postCommentRepository.countByPost(post);;
                    boolean liked = postLikeRepository.existsByPostAndUser(post, currentUser);
                    boolean bookmarked = postBookMarkRepository.existsByPostAndUser(post, currentUser);
                    return toDto(post, hashTags, numberOfLikes, numberOfComments, liked, bookmarked);
                })
                .collect(Collectors.toList());
    }
*/

    @Mapping(target = "authorProfileImageUrl", expression = "java(post.getUser().getUserProfile().getProfileImageUrl())")
    @Mapping(target = "hashTags", expression = "java(resolveHashTags(post))")
    @Mapping(target = "numberOfLikes", expression = "java(numberOfLikes)")
    @Mapping(target = "numberOfComments", expression = "java(numberOfComments)")
    @Mapping(target = "liked", expression = "java(liked)")
    @Mapping(target = "bookmarked", expression = "java(bookmarked)")
    @Mapping(target = "createdAt", expression = "java(post.getCreatedAt())")
    @Mapping(target = "updatedAt", expression = "java(post.getUpdatedAt())")
    abstract protected PostDto toDto(Post post, List<String> hashTags, long numberOfLikes, long numberOfComments, boolean liked, boolean bookmarked);

    protected List<String> resolveHashTags(Post post) {
        if (post.getPostHashTags() == null) {
            return Collections.emptyList();
        }

        return post.getPostHashTags().stream()
                .map(postHashTag -> postHashTag.getHashTag().getName())
                .toList();
    }

    private Map<Long, Long> getLikeCountMap(List<Post> posts) {
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return new HashMap<>();
        }

        List<PostLikeRepository.PostLikeCountProjection> results = postLikeRepository.countLikesByPostIds(postIds);

        return results.stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeCountProjection::getPostId,
                        PostLikeRepository.PostLikeCountProjection::getLikeCount
                ));
    }

    private Map<Long, Long> getCommentCountMap(List<Post> posts) {
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return new HashMap<>();
        }

        List<PostCommentRepository.PostCommentCountProjection> results = postCommentRepository.countCommentsByPostIds(postIds);

        return results.stream()
                .collect(Collectors.toMap(
                        PostCommentRepository.PostCommentCountProjection::getPostId,
                        PostCommentRepository.PostCommentCountProjection::getCommentCount
                ));

    }

    private Map<Long, Boolean> getLikedByMeMapByFeedIds(List<Post> posts, User currentUser) {
        if(currentUser == null) {
            return Collections.emptyMap();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();

        List<Long> likePostId = postLikeRepository.findLikedPostIdsByUserAndPostIds(currentUser.getId(), postIds);

        HashSet<Long> likedPostIdSet = new HashSet<>(likePostId);

        return posts.stream()
                .collect(Collectors.toMap(
                        Post::getId,
                        post -> likedPostIdSet.contains(post.getId())
                ));
    }

    private Map<Long, Boolean> getBookmarkedByMeMapByFeedIds(List<Post> posts, User currentUser) {
        if(currentUser == null) {
            return Collections.emptyMap();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();

        List<Long> bookmarkedPostIds = postBookMarkRepository.findBookmarkedPostIdsByUserAndPostIds(currentUser.getId(), postIds);

        HashSet<Long> bookmarkedPostIdSet = new HashSet<>(bookmarkedPostIds);

        return posts.stream()
                .collect(Collectors.toMap(
                        Post::getId,
                        post -> bookmarkedPostIdSet.contains(post.getId())
                ));
    }
}
package travel.mytravelplan.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.comment.entity.PostComment;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post")
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostBookMark> postBookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashTag> postHashTags = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Post(String content, List<String> imageUrls, User user) {
        this.content = content;
        this.imageUrls = imageUrls;
        this.user = user;
    }

    public static Post createPost(String content, List<String> imageUrls, User user, List<HashTag> hashTags) {
        Post post = Post.builder()
                .content(content)
                .imageUrls(imageUrls)
                .user(user)
                .build();

        List<PostHashTag> postHashTags = hashTags.stream()
                .map(PostHashTag::createPostHashTag)
                .toList();

        for (PostHashTag postHashTag : postHashTags) {
            post.addPostHashTag(postHashTag);
        }

        return post;
    }

    public void addPostHashTag(PostHashTag postHashTag) {
        this.postHashTags.add(postHashTag);
        postHashTag.setPost(this);
    }

    public void addComment(PostComment postComment) {
        this.postComments.add(postComment);
        postComment.setPost(this);
    }

    public void update(String content, List<String> imageUrls, List<HashTag> hashTags) {
        this.content = content;
        this.imageUrls = imageUrls;

        this.postHashTags.clear();

        List<PostHashTag> postHashTags = hashTags.stream()
                .map(PostHashTag::createPostHashTag)
                .toList();

        for (PostHashTag postHashTag : postHashTags) {
            this.addPostHashTag(postHashTag);
        }
    }
}

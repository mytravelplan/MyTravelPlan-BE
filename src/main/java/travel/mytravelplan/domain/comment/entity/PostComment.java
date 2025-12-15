package travel.mytravelplan.domain.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.global.common.entity.BaseEntity;
import travel.mytravelplan.domain.post.entity.Post;
import travel.mytravelplan.domain.user.entity.User;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private PostComment(String content, Post post, User user) {
        this.user = user;
        this.post = post;
        this.content = content;
    }

    public static PostComment createPostComment(String content, Post post, User user) {
        return PostComment.builder()
                .content(content)
                .post(post)
                .user(user)
                .build();
    }

    public void update(String content) {
        this.content = content;
    }

}

package travel.mytravelplan.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostHashTag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private HashTag hashTag;

    @Builder(access = AccessLevel.PRIVATE)
    private PostHashTag(Post post, HashTag hashTag) {
        this.post = post;
        this.hashTag = hashTag;
    }

    public static PostHashTag createPostHashTag(HashTag hashTag) {
        return PostHashTag.builder()
                .hashTag(hashTag)
                .build();
    }
}

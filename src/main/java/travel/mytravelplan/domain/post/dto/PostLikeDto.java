package travel.mytravelplan.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostLikeDto {
    private Long postId;
    private Long userId;
    private boolean liked;

    @Builder
    private PostLikeDto(Long postId, Long userId, boolean liked) {
        this.postId = postId;
        this.userId = userId;
        this.liked = liked;
    }
}

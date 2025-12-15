package travel.mytravelplan.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostBookMarkDto {
    private Long postId;
    private Long userId;
    private boolean bookmarked;

    @Builder
    private PostBookMarkDto(Long postId, Long userId, boolean bookmarked) {
        this.postId = postId;
        this.userId = userId;
        this.bookmarked = bookmarked;
    }
}

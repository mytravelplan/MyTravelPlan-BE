package travel.mytravelplan.domain.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCommentCreateRequestDto {
    private String content;

    @Builder
    private PostCommentCreateRequestDto(String content) {
        this.content = content;
    }
}

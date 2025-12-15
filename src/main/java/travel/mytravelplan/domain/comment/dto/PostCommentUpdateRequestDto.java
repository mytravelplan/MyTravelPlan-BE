package travel.mytravelplan.domain.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCommentUpdateRequestDto {
    private String content;

    @Builder
    private PostCommentUpdateRequestDto(String content) {
        this.content = content;
    }
}

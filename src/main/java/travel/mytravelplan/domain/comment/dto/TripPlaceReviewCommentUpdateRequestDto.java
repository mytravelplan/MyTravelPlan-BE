package travel.mytravelplan.domain.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TripPlaceReviewCommentUpdateRequestDto {
    private String content;

    @Builder
    private TripPlaceReviewCommentUpdateRequestDto(String content) {
        this.content = content;
    }
}

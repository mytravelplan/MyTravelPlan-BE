package travel.mytravelplan.domain.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TripPlaceReviewCommentCreateRequestDto {
    private String content;

    @Builder
    private TripPlaceReviewCommentCreateRequestDto(String content) {
        this.content = content;
    }
}

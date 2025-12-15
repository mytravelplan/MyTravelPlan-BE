package travel.mytravelplan.domain.review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class TripPlaceReviewCreateRequestDto {
    private String content;
    private BigDecimal rating;

    @Builder
    private TripPlaceReviewCreateRequestDto(String content, BigDecimal rating) {
        this.content = content;
        this.rating = rating;
    }
}

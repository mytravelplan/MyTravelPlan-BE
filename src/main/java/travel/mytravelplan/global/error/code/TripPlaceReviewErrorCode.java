package travel.mytravelplan.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripPlaceReviewErrorCode implements ErrorCode {
    TRIP_PLACE_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "TRIP_PLACE_REVIEW-01", "여행 장소 리뷰를 찾을 수 없습니다."),
    TRIP_PLACE_REVIEW_NOT_BELONG_TO_TRIP_PLACE(HttpStatus.BAD_REQUEST, "TRIP_PLACE_REVIEW-02", "여행 장소 리뷰가 해당 여행 장소에 속하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

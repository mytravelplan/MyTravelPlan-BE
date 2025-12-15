package travel.mytravelplan.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.exception.TripPlaceException;
import travel.mytravelplan.domain.place.repository.TripPlaceRepository;
import travel.mytravelplan.domain.review.dto.*;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.review.entity.TripPlaceReviewLike;
import travel.mytravelplan.domain.review.exception.TripPlaceReviewException;
import travel.mytravelplan.domain.review.mapper.TripPlaceReviewLikeMapper;
import travel.mytravelplan.domain.review.mapper.TripPlaceReviewMapper;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewLikeRepository;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.TripPlaceErrorCode;
import travel.mytravelplan.global.error.code.TripPlaceReviewErrorCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TripPlaceReviewService {
    private final TripPlaceRepository tripPlaceRepository;
    private final TripPlaceReviewRepository tripPlaceReviewRepository;
    private final TripPlaceReviewLikeRepository tripPlaceReviewLikeRepository;
    private final TripPlaceReviewMapper tripPlaceReviewMapper;
    private final TripPlaceReviewLikeMapper tripPlaceReviewLikeMapper;

    @Transactional
    public TripPlaceReviewDto createTripPlaceReview(User currentUser, Long tripPlaceId, TripPlaceReviewCreateRequestDto tripPlaceReviewCreateRequestDto) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = TripPlaceReview.createTripPlaceReview(currentUser, tripPlace, tripPlaceReviewCreateRequestDto.getRating(), tripPlaceReviewCreateRequestDto.getContent());

        tripPlaceReviewRepository.save(tripPlaceReview);

        return tripPlaceReviewMapper.toDto(tripPlaceReview, currentUser);
    }

    public TripPlaceReviewDto getTripPlaceReview(User currentUser, Long tripPlaceId, Long tripPlaceReviewId) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);

        return tripPlaceReviewMapper.toDto(tripPlaceReview, currentUser);
    }

    public CursorPageResponseDto<TripPlaceReviewDto> getTripPlaceReviews(User currentUser, Long tripPlaceId, String keyword, boolean imgOnly, BigDecimal rating, String orderBy, String direction, String cursor, Long after, int limit) {
        List<TripPlaceReview> tripPlaceReviews = tripPlaceReviewRepository.findAllByCursor(tripPlaceId, keyword, imgOnly, rating, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = tripPlaceReviews.size() > limit;

        List<TripPlaceReview> pagedTripPlaceReviews = hasNext ? tripPlaceReviews.subList(0, limit) : tripPlaceReviews;

        List<TripPlaceReviewDto> tripPlaceReviewDtos = pagedTripPlaceReviews.stream()
                .map(tripPlaceReview -> tripPlaceReviewMapper.toDto(tripPlaceReview, currentUser))
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            TripPlaceReview lastReview = pagedTripPlaceReviews.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastReview.getCreatedAt().toString();
            } else if (orderBy.equals("rating")) {
                nextCursor = String.valueOf(lastReview.getRating());
            }

            nextAfter = lastReview.getId();
        }

        return CursorPageResponseDto.<TripPlaceReviewDto>builder()
                .content(tripPlaceReviewDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(tripPlaceReviewDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public TripPlaceReviewDto updateTripPlaceReview(User currentUser, Long tripPlaceId, Long tripPlaceReviewId, TripPlaceReviewUpdateRequestDto tripPlaceReviewUpdateRequestDto) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);

        tripPlaceReview.update(
                tripPlaceReviewUpdateRequestDto.getContent(),
                tripPlaceReviewUpdateRequestDto.getRating()
        );

        return tripPlaceReviewMapper.toDto(tripPlaceReview, currentUser);
    }

    @Transactional
    public void deleteTripPlaceReview(Long tripPlaceId, Long tripPlaceReviewId) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);
        tripPlaceReviewRepository.delete(tripPlaceReview);
    }

    @Transactional
    public TripPlaceReviewLikeDto likeTripPlaceReview(User currentUser, Long tripPlaceId, Long tripPlaceReviewId) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);

        Optional<TripPlaceReviewLike> tripPlaceReviewLikeOptional = tripPlaceReviewLikeRepository.findByTripPlaceReviewAndUser(tripPlaceReview, currentUser);

        TripPlaceReviewLike tripPlaceReviewLike;
        boolean isLiked;

        if (tripPlaceReviewLikeOptional.isPresent()) {
            tripPlaceReviewLike = tripPlaceReviewLikeOptional.get();
            tripPlaceReviewLikeRepository.delete(tripPlaceReviewLike);
            isLiked = false;
        } else {
            tripPlaceReviewLike = TripPlaceReviewLike.createTripPlaceReviewLike(tripPlaceReview, currentUser);
            tripPlaceReviewLikeRepository.save(tripPlaceReviewLike);
            isLiked = true;
        }

        return tripPlaceReviewLikeMapper.toDto(tripPlaceReviewLike, isLiked);
    }

/*
    public CursorPageResponseDto<PopularTripPlaceReviewDto> getPopularTripPlaceReviews(Long tripPlaceId, Period period, String direction, String cursor, Long after, int limit) {
        return null;
    }
*/

    private void validateTripPlaceReviewBelongsToTripPlace(TripPlaceReview tripPlaceReview, TripPlace tripPlace) {
        if(!tripPlaceReview.getTripPlace().equals(tripPlace)) {
            throw new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_BELONG_TO_TRIP_PLACE);
        }
    }
}

package travel.mytravelplan.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentDto;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentCreateRequestDto;
import travel.mytravelplan.domain.comment.dto.TripPlaceReviewCommentUpdateRequestDto;
import travel.mytravelplan.domain.comment.entity.TripPlaceReviewComment;
import travel.mytravelplan.domain.comment.exception.TripPlaceReviewCommentException;
import travel.mytravelplan.domain.comment.mapper.TripPlaceReviewCommentMapper;
import travel.mytravelplan.domain.comment.repository.TripPlaceReviewCommentRepository;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.exception.TripPlaceException;
import travel.mytravelplan.domain.place.repository.TripPlaceRepository;
import travel.mytravelplan.domain.review.entity.TripPlaceReview;
import travel.mytravelplan.domain.review.exception.TripPlaceReviewException;
import travel.mytravelplan.domain.review.repository.TripPlaceReviewRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.*;
import travel.mytravelplan.global.error.code.TripPlaceReviewCommentErrorCode;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TripPlaceReviewCommentService {
    private final TripPlaceRepository tripPlaceRepository;
    private final TripPlaceReviewRepository tripPlaceReviewRepository;
    private final TripPlaceReviewCommentRepository tripPlaceReviewCommentRepository;
    private final TripPlaceReviewCommentMapper tripPlaceReviewCommentMapper;

    @Transactional
    public TripPlaceReviewCommentDto createTripPlaceReviewComment(User currentUser, Long tripPlaceId, Long tripPlaceReviewId, TripPlaceReviewCommentCreateRequestDto tripPlaceReviewCommentCreateRequestDto) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);

        TripPlaceReviewComment tripPlaceReviewComment = TripPlaceReviewComment.createTripPlaceReviewComment(tripPlaceReviewCommentCreateRequestDto.getContent(), tripPlaceReview, currentUser);

        tripPlaceReviewCommentRepository.save(tripPlaceReviewComment);

        return tripPlaceReviewCommentMapper.toDto(tripPlaceReviewComment);
    }

    public CursorPageResponseDto<TripPlaceReviewCommentDto> getTripPlaceReviewComments(Long tripPlaceId, Long tripPlaceReviewId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);

        List<TripPlaceReviewComment> tripPlaceReviewComments = tripPlaceReviewCommentRepository.findAllByCursor(tripPlaceReviewId, keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = tripPlaceReviewComments.size() > limit;

        List<TripPlaceReviewComment> pagedTripPlaceReviewComments = hasNext ? tripPlaceReviewComments.subList(0, limit) : tripPlaceReviewComments;

        List<TripPlaceReviewCommentDto> tripPlaceReviewCommentDtos = pagedTripPlaceReviewComments.stream()
                .map(tripPlaceReviewCommentMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            TripPlaceReviewComment lastComment = pagedTripPlaceReviewComments.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastComment.getCreatedAt().toString();
            }

            nextAfter = lastComment.getId();
        }

        return CursorPageResponseDto.<TripPlaceReviewCommentDto>builder()
                .content(tripPlaceReviewCommentDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(tripPlaceReviewCommentDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public TripPlaceReviewCommentDto getTripPlaceReviewComment(Long tripPlaceId, Long tripPlaceReviewId, Long tripPlaceCommentId) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);

        TripPlaceReviewComment tripPlaceReviewComment = tripPlaceReviewCommentRepository.findById(tripPlaceCommentId)
                .orElseThrow(() -> new TripPlaceReviewCommentException(TripPlaceReviewCommentErrorCode.TRIP_PLACE_REVIEW_COMMENT_NOT_FOUND));


        return tripPlaceReviewCommentMapper.toDto(tripPlaceReviewComment);
    }

    @Transactional
    public TripPlaceReviewCommentDto updateTripPlaceReviewComment(Long tripPlaceId, Long tripPlaceReviewId, Long tripPlaceCommentId, TripPlaceReviewCommentUpdateRequestDto tripPlaceReviewCommentUpdateRequestDto) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);

        TripPlaceReviewComment tripPlaceReviewComment = tripPlaceReviewCommentRepository.findById(tripPlaceCommentId)
                .orElseThrow(() -> new TripPlaceReviewCommentException(TripPlaceReviewCommentErrorCode.TRIP_PLACE_REVIEW_COMMENT_NOT_FOUND));

        tripPlaceReviewComment.update(tripPlaceReviewCommentUpdateRequestDto.getContent());

        return tripPlaceReviewCommentMapper.toDto(tripPlaceReviewComment);
    }

    @Transactional
    public void deleteTripPlaceReviewComment(Long tripPlaceId, Long tripPlaceReviewId, Long tripPlaceCommentId) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        TripPlaceReview tripPlaceReview = tripPlaceReviewRepository.findById(tripPlaceReviewId)
                .orElseThrow(() -> new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_FOUND));

        validateTripPlaceReviewBelongsToTripPlace(tripPlaceReview, tripPlace);

        TripPlaceReviewComment tripPlaceReviewComment = tripPlaceReviewCommentRepository.findById(tripPlaceCommentId)
                .orElseThrow(() -> new TripPlaceReviewCommentException(TripPlaceReviewCommentErrorCode.TRIP_PLACE_REVIEW_COMMENT_NOT_FOUND));

        tripPlaceReviewCommentRepository.delete(tripPlaceReviewComment);
    }

    private void validateTripPlaceReviewBelongsToTripPlace(TripPlaceReview tripPlaceReview, TripPlace tripPlace) {
        if (!tripPlaceReview.getTripPlace().equals(tripPlace)) {
            throw new TripPlaceReviewException(TripPlaceReviewErrorCode.TRIP_PLACE_REVIEW_NOT_BELONG_TO_TRIP_PLACE);
        }
    }
}

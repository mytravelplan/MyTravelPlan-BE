package travel.mytravelplan.domain.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.place.dto.*;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.entity.TripPlaceBookMark;
import travel.mytravelplan.domain.place.exception.TripPlaceException;
import travel.mytravelplan.domain.place.mapper.TripPlaceBookMarkMapper;
import travel.mytravelplan.domain.place.mapper.TripPlaceMapper;
import travel.mytravelplan.domain.place.repository.TripPlaceBookMarkRepository;
import travel.mytravelplan.domain.place.repository.TripPlaceRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.enums.Period;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.TripPlaceErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TripPlaceService {
    private final TripPlaceRepository tripPlaceRepository;
    private final TripPlaceBookMarkRepository tripPlaceBookMarkRepository;
    private final TripPlaceMapper tripPlaceMapper;
    private final TripPlaceBookMarkMapper tripPlaceBookMarkMapper;

    @Transactional
    public TripPlaceDto createTripPlace(User currentUser, TripPlaceCreateRequestDto tripPlaceCreateRequestDto) {
        TripPlace tripPlace = TripPlace.createTripPlace(
                tripPlaceCreateRequestDto.getName(),
                tripPlaceCreateRequestDto.getAddress(),
                tripPlaceCreateRequestDto.getDescription(),
                tripPlaceCreateRequestDto.getLatitude(),
                tripPlaceCreateRequestDto.getLongitude(),
                tripPlaceCreateRequestDto.getCategory(),
                tripPlaceCreateRequestDto.getExternalUrl()
        );

        tripPlaceRepository.save(tripPlace);

        return tripPlaceMapper.toDto(tripPlace, currentUser);
    }

    public CursorPageResponseDto<TripPlaceDto> getTripPlaces(User currentUser, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<TripPlace> tripPlaces = tripPlaceRepository.findAllByCursor(keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = tripPlaces.size() > limit;

        List<TripPlace> pagedTripPlaces = hasNext ? tripPlaces.subList(0, limit) : tripPlaces;

        List<TripPlaceDto> tripPlaceDtos = pagedTripPlaces.stream()
                .map(tripPlace -> tripPlaceMapper.toDto(tripPlace, currentUser))
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            TripPlace lastTripPlace = pagedTripPlaces.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastTripPlace.getCreatedAt().toString();
            }

            nextAfter = lastTripPlace.getId();
        }

        return CursorPageResponseDto.<TripPlaceDto>builder()
                .content(tripPlaceDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(tripPlaceDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public TripPlaceDto getTripPlace(User currentUser, Long tripPlaceId) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        return tripPlaceMapper.toDto(tripPlace, currentUser);
    }

    @Transactional
    public TripPlaceDto updateTripPlace(User currentUser, Long tripPlaceId, TripPlaceUpdateRequestDto tripPlaceUpdateRequestDto) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        tripPlace.update(
                tripPlaceUpdateRequestDto.getName(),
                tripPlaceUpdateRequestDto.getAddress(),
                tripPlaceUpdateRequestDto.getDescription(),
                tripPlaceUpdateRequestDto.getLatitude(),
                tripPlaceUpdateRequestDto.getLongitude(),
                tripPlaceUpdateRequestDto.getCategory(),
                tripPlaceUpdateRequestDto.getExternalUrl()
        );

        return tripPlaceMapper.toDto(tripPlace, currentUser);
    }

    @Transactional
    public void deleteTripPlace(Long tripPlaceId) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        tripPlaceRepository.delete(tripPlace);
    }

    @Transactional
    public TripPlaceBookMarkDto bookmarkTripPlace(User currentUser, Long tripPlaceId) {
        TripPlace tripPlace = tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new TripPlaceException(TripPlaceErrorCode.TRIP_PLACE_NOT_FOUND));

        Optional<TripPlaceBookMark> tripPlaceBookMarkOptional = tripPlaceBookMarkRepository.findByTripPlaceAndUser(tripPlace, currentUser);

        TripPlaceBookMark tripPlaceBookMark;
        boolean isBookmarked;

        if (tripPlaceBookMarkOptional.isPresent()) {
            tripPlaceBookMark = tripPlaceBookMarkOptional.get();
            tripPlaceBookMarkRepository.delete(tripPlaceBookMark);
            isBookmarked = false;
        } else {
            tripPlaceBookMark = TripPlaceBookMark.createTripPlaceBookMark(tripPlace, currentUser);
            tripPlaceBookMarkRepository.save(tripPlaceBookMark);
            isBookmarked = true;
        }

        return tripPlaceBookMarkMapper.toDto(tripPlaceBookMark, isBookmarked);
    }

/*
    public CursorPageResponseDto<PopularTripPlaceDto> getPopularTripPlaces(Period period, String direction, String cursor, Long after, int limit) {
        return null;
    }
*/
}

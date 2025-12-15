package travel.mytravelplan.domain.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.place.dto.CustomPlaceCreateRequestDto;
import travel.mytravelplan.domain.place.dto.CustomPlaceDto;
import travel.mytravelplan.domain.place.dto.CustomPlaceUpdateRequestDto;
import travel.mytravelplan.domain.place.entity.CustomPlace;
import travel.mytravelplan.domain.place.exception.CustomPlaceException;
import travel.mytravelplan.domain.place.mapper.CustomPlaceMapper;
import travel.mytravelplan.domain.place.repository.CustomPlaceRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.CustomPlaceErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomPlaceService {
    private final CustomPlaceRepository customPlaceRepository;
    private final CustomPlaceMapper customPlaceMapper;

    @Transactional
    public CustomPlaceDto createCustomPlace(User currentUser, CustomPlaceCreateRequestDto customPlaceCreateRequestDto) {
        CustomPlace customPlace = CustomPlace.createCustomPlace(
                customPlaceCreateRequestDto.getName(),
                customPlaceCreateRequestDto.getAddress(),
                customPlaceCreateRequestDto.getDescription(),
                customPlaceCreateRequestDto.getLatitude(),
                customPlaceCreateRequestDto.getLongitude(),
                customPlaceCreateRequestDto.getCategory(),
                currentUser
        );

        customPlaceRepository.save(customPlace);

        return customPlaceMapper.toDto(customPlace);
    }

    public CustomPlaceDto getCustomPlace(Long customPlaceId) {
        CustomPlace customPlace = customPlaceRepository.findById(customPlaceId)
                .orElseThrow(() -> new CustomPlaceException(CustomPlaceErrorCode.CUSTOM_PLACE_NOT_FOUND));

        return customPlaceMapper.toDto(customPlace);
    }

    public CursorPageResponseDto<CustomPlaceDto> getCustomPlaces(User currentUser, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<CustomPlace> customPlaces = customPlaceRepository.findAllByCursor(currentUser.getUsername(), keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = customPlaces.size() > limit;

        List<CustomPlace> pagedCustomPlaces = hasNext ? customPlaces.subList(0, limit) : customPlaces;

        List<CustomPlaceDto> customPlaceDtos = pagedCustomPlaces.stream()
                .map(customPlaceMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            CustomPlace lastCustomPlace = pagedCustomPlaces.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastCustomPlace.getCreatedAt().toString();
            }

            nextAfter = lastCustomPlace.getId();
        }

        return CursorPageResponseDto.<CustomPlaceDto>builder()
                .content(customPlaceDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(customPlaceDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public CustomPlaceDto updateCustomPlace(Long customPlaceId, CustomPlaceUpdateRequestDto customPlaceUpdateRequestDto) {
        CustomPlace customPlace = customPlaceRepository.findById(customPlaceId)
                .orElseThrow(() -> new CustomPlaceException(CustomPlaceErrorCode.CUSTOM_PLACE_NOT_FOUND));

        customPlace.update(
                customPlaceUpdateRequestDto.getName(),
                customPlaceUpdateRequestDto.getAddress(),
                customPlaceUpdateRequestDto.getDescription(),
                customPlaceUpdateRequestDto.getLatitude(),
                customPlaceUpdateRequestDto.getLongitude(),
                customPlaceUpdateRequestDto.getCategory()
        );

        return customPlaceMapper.toDto(customPlace);
    }

    @Transactional
    public void deleteCustomPlace(Long customPlaceId) {
        CustomPlace customPlace = customPlaceRepository.findById(customPlaceId)
                .orElseThrow(() -> new CustomPlaceException(CustomPlaceErrorCode.CUSTOM_PLACE_NOT_FOUND));

        customPlaceRepository.delete(customPlace);
    }
}

package travel.mytravelplan.domain.checklist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.checklist.dto.*;
import travel.mytravelplan.domain.checklist.entity.CheckList;
import travel.mytravelplan.domain.checklist.entity.PersonalCheckList;
import travel.mytravelplan.domain.checklist.entity.SharedCheckList;
import travel.mytravelplan.domain.checklist.enums.CheckListType;
import travel.mytravelplan.domain.checklist.exception.CheckListException;
import travel.mytravelplan.domain.checklist.mapper.CheckListMapper;
import travel.mytravelplan.domain.checklist.repository.CheckListRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.exception.TripJoinException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.CheckListErrorCode;
import travel.mytravelplan.global.error.code.TripErrorCode;
import travel.mytravelplan.global.error.code.TripJoinErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CheckListService {
    private final CheckListRepository checkListRepository;
    private final TripRepository tripRepository;
    private final TripJoinRepository tripJoinRepository;
    private final CheckListMapper checkListMapper;

    @Transactional
    public CheckListDto createCheckList(User currentUser, Long tripId, CheckListCreateRequestDto checkListCreateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckListType checkListType = checkListCreateRequestDto.getCheckListType();

        CheckList checkList = null;

        if (checkListType.equals(CheckListType.SHARED)) {
             checkList = SharedCheckList.createSharedCheckList(checkListCreateRequestDto.getName(), trip);
        } else if (checkListType.equals(CheckListType.PERSONAL)) {
            TripJoin tripJoin = tripJoinRepository.findByUserAndTrip(currentUser, trip)
                    .orElseThrow(() -> new TripJoinException(TripJoinErrorCode.TRIP_JOIN_NOT_FOUND));
            checkList = PersonalCheckList.createPersonalCheckList(checkListCreateRequestDto.getName(), trip, tripJoin);
        }

        checkListRepository.save(checkList);

        return checkListMapper.toDto(checkList);
    }

    public CheckListDto getCheckList(Long tripId, Long checkListId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_FOUND));

        validateCheckListBelongsToTrip(checkList, trip);

        return checkListMapper.toDto(checkList);
    }

    public CursorPageResponseDto<CheckListDto> getCheckLists(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<CheckList> checkLists = checkListRepository.findAllByCursor(tripId, keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = checkLists.size() > limit;

        List<CheckList> pagedCheckLists = hasNext ? checkLists.subList(0, limit) : checkLists;

        List<CheckListDto> checkListDtos = pagedCheckLists.stream()
                .map(checkListMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            CheckList lastCheckList = pagedCheckLists.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastCheckList.getCreatedAt().toString();
            }

            nextAfter = lastCheckList.getId();
        }

        return CursorPageResponseDto.<CheckListDto>builder()
                .content(checkListDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(checkListDtos.size())
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public CheckListDto updateCheckList(Long tripId, Long checkListId, CheckListUpdateRequestDto checkListUpdateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_FOUND));

        validateCheckListBelongsToTrip(checkList, trip);

        if (checkList instanceof SharedCheckList) {
            ((SharedCheckList) checkList).update(checkListUpdateRequestDto.getName());
        } else if (checkList instanceof PersonalCheckList) {
            ((PersonalCheckList) checkList).update(checkListUpdateRequestDto.getName());
        }

        return checkListMapper.toDto(checkList);
    }

    @Transactional
    public void deleteCheckList(Long tripId, Long checkListId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_FOUND));

        validateCheckListBelongsToTrip(checkList, trip);

        checkListRepository.delete(checkList);
    }

    private void validateCheckListBelongsToTrip(CheckList checkList, Trip trip) {
        if (!checkList.getTrip().equals(trip)) {
            throw new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_BELONG_TO_TRIP);
        }
    }
}

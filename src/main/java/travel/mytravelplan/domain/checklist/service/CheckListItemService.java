package travel.mytravelplan.domain.checklist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.checklist.dto.*;
import travel.mytravelplan.domain.checklist.entity.*;
import travel.mytravelplan.domain.checklist.enums.CheckListType;
import travel.mytravelplan.domain.checklist.exception.CheckListException;
import travel.mytravelplan.domain.checklist.exception.CheckListItemException;
import travel.mytravelplan.domain.checklist.mapper.CheckListItemMapper;
import travel.mytravelplan.domain.checklist.repository.*;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.exception.TripJoinException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.CheckListErrorCode;
import travel.mytravelplan.global.error.code.CheckListItemErrorCode;
import travel.mytravelplan.global.error.code.TripErrorCode;
import travel.mytravelplan.global.error.code.TripJoinErrorCode;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CheckListItemService {
    private final CheckListRepository checkListRepository;
    private final TripRepository tripRepository;
    private final CheckListItemMapper checkListItemMapper;
    private final PersonalCheckListItemRepository personalCheckListItemRepository;
    private final SharedCheckListItemRepository sharedCheckListItemRepository;
    private final TripJoinRepository tripJoinRepository;
    private final SharedCheckListItemCheckRepository sharedCheckListItemCheckRepository;

    @Transactional
    public CheckListItemDto createCheckListItem(User currentUser, Long tripId, Long checkListId, CheckListItemCreateRequestDto checkListCreateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_FOUND));

        validateCheckListBelongsToTrip(checkList, trip);

        CheckListItem checkListItem;

        CheckListType checkListType = checkListCreateRequestDto.getCheckListType();

        if (checkList instanceof SharedCheckList) {
            if (!checkListType.equals(CheckListType.SHARED)) {
                throw new CheckListException(CheckListErrorCode.CHECK_LIST_TYPE_MISMATCH);
            }

            checkListItem = SharedCheckListItem.createSharedCheckListItem(checkListCreateRequestDto.getText(), (SharedCheckList) checkList);

            TripJoin tripJoin = tripJoinRepository.findByUserAndTrip(currentUser, trip)
                    .orElseThrow(() -> new TripJoinException(TripJoinErrorCode.TRIP_JOIN_NOT_FOUND));

            SharedCheckListItemCheck sharedCheckListItemCheck = SharedCheckListItemCheck.createSharedCheckListItemCheck(tripJoin, (SharedCheckListItem) checkListItem);

            sharedCheckListItemRepository.save((SharedCheckListItem) checkListItem);
            sharedCheckListItemCheckRepository.save(sharedCheckListItemCheck);
            return checkListItemMapper.toDto((SharedCheckListItem) checkListItem);
        } else if (checkList instanceof PersonalCheckList) {
            if (!checkListType.equals(CheckListType.PERSONAL)) {
                throw new CheckListException(CheckListErrorCode.CHECK_LIST_TYPE_MISMATCH);
            }

            checkListItem = PersonalCheckListItem.createPersonalCheckListItem(checkListCreateRequestDto.getText(), (PersonalCheckList) checkList);
            personalCheckListItemRepository.save((PersonalCheckListItem) checkListItem);
            return checkListItemMapper.toDto((PersonalCheckListItem) checkListItem);
        }
        return null;
    }

    public CheckListItemDto getCheckListItem(Long tripId, Long checkListId, Long checkListItemId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_FOUND));

        validateCheckListBelongsToTrip(checkList, trip);

        CheckListItem checkListItem;

        if (checkList instanceof SharedCheckList) {
            checkListItem = sharedCheckListItemRepository.findById(checkListItemId)
                    .orElseThrow(() -> new CheckListItemException(CheckListItemErrorCode.CHECK_LIST_ITEM_NOT_FOUND));
            validateCheckListItemBelongsToCheckList(checkListItem, checkList);
            return checkListItemMapper.toDto((SharedCheckListItem) checkListItem);
        } else if (checkList instanceof PersonalCheckList) {
            checkListItem = personalCheckListItemRepository.findById(checkListItemId)
                    .orElseThrow(() -> new CheckListItemException(CheckListItemErrorCode.CHECK_LIST_ITEM_NOT_FOUND));
            validateCheckListItemBelongsToCheckList(checkListItem, checkList);
            return checkListItemMapper.toDto((PersonalCheckListItem) checkListItem);
        }

        return null;
    }

    public CursorPageResponseDto<? extends CheckListItemDto> getCheckListItems(Long tripId, Long checkListId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_FOUND));

        validateCheckListBelongsToTrip(checkList, trip);

        if (checkList instanceof SharedCheckList) {
            List<SharedCheckListItem> sharedCheckListItems = sharedCheckListItemRepository.findAllByCursor(checkList.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

            boolean hasNext = sharedCheckListItems.size() > limit;

            List<SharedCheckListItem> pagedSharedCheckListItems = hasNext ? sharedCheckListItems.subList(0, limit) : sharedCheckListItems;

            List<SharedCheckListItemDto> sharedCheckListItemDtos = pagedSharedCheckListItems.stream()
                    .map(checkListItemMapper::toDto)
                    .toList();

            String nextCursor = null;
            Long nextAfter = null;

            if (hasNext) {
                SharedCheckListItem lastSharedCheckListItem = pagedSharedCheckListItems.getLast();

                if (orderBy.equals("createdAt")) {
                    nextCursor = lastSharedCheckListItem.getCreatedAt().toString();
                }

                nextAfter = lastSharedCheckListItem.getId();
            }

            return CursorPageResponseDto.<SharedCheckListItemDto>builder()
                    .content(sharedCheckListItemDtos)
                    .nextCursor(nextCursor)
                    .nextAfter(nextAfter)
                    .size(sharedCheckListItemDtos.size())
                    .hasNext(hasNext)
                    .build();
        }else if (checkList instanceof PersonalCheckList) {
            List<PersonalCheckListItem> personalCheckListItems = personalCheckListItemRepository.findAllByCursor(checkList.getId(), keyword, orderBy, direction, cursor, after, limit + 1);

            boolean hasNext = personalCheckListItems.size() > limit;

            List<PersonalCheckListItem> pagedPersonalCheckListItems = hasNext ? personalCheckListItems.subList(0, limit) : personalCheckListItems;

            List<PersonalCheckListItemDto> personalCheckListItemDtos = pagedPersonalCheckListItems.stream()
                    .map(checkListItemMapper::toDto)
                    .toList();

            String nextCursor = null;
            Long nextAfter = null;

            if (hasNext) {
                PersonalCheckListItem lastPersonalCheckListItem = pagedPersonalCheckListItems.getLast();

                if (orderBy.equals("createdAt")) {
                    nextCursor = lastPersonalCheckListItem.getCreatedAt().toString();
                }

                nextAfter = lastPersonalCheckListItem.getId();
            }

            return CursorPageResponseDto.<PersonalCheckListItemDto>builder()
                    .content(personalCheckListItemDtos)
                    .nextCursor(nextCursor)
                    .nextAfter(nextAfter)
                    .size(personalCheckListItemDtos.size())
                    .hasNext(hasNext)
                    .build();
        }

        return null;
    }

    @Transactional
    public CheckListItemDto updateCheckListItem(Long tripId, Long checkListId, Long checkListItemId, CheckListItemUpdateRequestDto checkListItemUpdateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_FOUND));

        validateCheckListBelongsToTrip(checkList, trip);

        CheckListItem checkListItem;

        CheckListType checkListType = checkListItemUpdateRequestDto.getCheckListType();

        if (checkList instanceof SharedCheckList) {
            if (!checkListType.equals(CheckListType.SHARED)) {
                throw new CheckListException(CheckListErrorCode.CHECK_LIST_TYPE_MISMATCH);
            }
            checkListItem = sharedCheckListItemRepository.findById(checkListItemId)
                    .orElseThrow(() -> new CheckListItemException(CheckListItemErrorCode.CHECK_LIST_ITEM_NOT_FOUND));
            validateCheckListItemBelongsToCheckList(checkListItem, checkList);
            ((SharedCheckListItem) checkListItem).update(checkListItemUpdateRequestDto.getText());
            return checkListItemMapper.toDto((SharedCheckListItem) checkListItem);
        } else if (checkList instanceof PersonalCheckList) {
            if (!checkListType.equals(CheckListType.PERSONAL)) {
                throw new CheckListException(CheckListErrorCode.CHECK_LIST_TYPE_MISMATCH);
            }
            checkListItem = personalCheckListItemRepository.findById(checkListItemId)
                    .orElseThrow(() -> new CheckListItemException(CheckListItemErrorCode.CHECK_LIST_ITEM_NOT_FOUND));
            validateCheckListItemBelongsToCheckList(checkListItem, checkList);
            ((PersonalCheckListItem) checkListItem).update(checkListItemUpdateRequestDto.getText(), checkListItemUpdateRequestDto.isChecked());
            return checkListItemMapper.toDto((PersonalCheckListItem) checkListItem);
        }

        return null;
    }

    @Transactional
    public void deleteCheckListItem(Long tripId, Long checkListId, Long checkListItemId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        CheckList checkList = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_FOUND));

        validateCheckListBelongsToTrip(checkList, trip);

        CheckListItem checkListItem;

        if (checkList instanceof SharedCheckList) {
            checkListItem = sharedCheckListItemRepository.findById(checkListItemId)
                    .orElseThrow(() -> new CheckListItemException(CheckListItemErrorCode.CHECK_LIST_ITEM_NOT_FOUND));
            validateCheckListItemBelongsToCheckList(checkListItem, checkList);
            sharedCheckListItemRepository.delete((SharedCheckListItem) checkListItem);
        } else if (checkList instanceof PersonalCheckList) {
            checkListItem = personalCheckListItemRepository.findById(checkListItemId)
                    .orElseThrow(() -> new CheckListItemException(CheckListItemErrorCode.CHECK_LIST_ITEM_NOT_FOUND));
            validateCheckListItemBelongsToCheckList(checkListItem, checkList);
            personalCheckListItemRepository.delete((PersonalCheckListItem) checkListItem);
        }
    }

    private void validateCheckListBelongsToTrip(CheckList checkList, Trip trip) {
        if (!checkList.getTrip().equals(trip)) {
            throw new CheckListException(CheckListErrorCode.CHECK_LIST_NOT_BELONG_TO_TRIP);
        }
    }
    
    private void validateCheckListItemBelongsToCheckList(CheckListItem checkListItem, CheckList checkList) {
        if(checkListItem instanceof SharedCheckListItem) {
            if (!((SharedCheckListItem) checkListItem).getSharedCheckList().equals(checkList)) {
                throw new CheckListItemException(CheckListItemErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECKLIST);
            }
        } else if(checkListItem instanceof PersonalCheckListItem) {
            if (!((PersonalCheckListItem) checkListItem).getPersonalCheckList().equals(checkList)) {
                throw new CheckListItemException(CheckListItemErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECKLIST);
            }
        }
    }
}

package travel.mytravelplan.domain.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.diary.dto.DiaryCreateRequestDto;
import travel.mytravelplan.domain.diary.dto.DiaryDto;
import travel.mytravelplan.domain.diary.dto.DiaryUpdateRequestDto;
import travel.mytravelplan.domain.diary.entity.Diary;
import travel.mytravelplan.domain.diary.exception.DiaryException;
import travel.mytravelplan.domain.diary.mapper.DiaryMapper;
import travel.mytravelplan.domain.diary.repository.DiaryRepository;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.domain.trip.exception.TripException;
import travel.mytravelplan.domain.trip.exception.TripJoinException;
import travel.mytravelplan.domain.trip.repository.TripJoinRepository;
import travel.mytravelplan.domain.trip.repository.TripRepository;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;
import travel.mytravelplan.global.error.code.DiaryErrorCode;
import travel.mytravelplan.global.error.code.TripErrorCode;
import travel.mytravelplan.global.error.code.TripJoinErrorCode;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final TripRepository tripRepository;
    private final DiaryMapper diaryMapper;
    private final TripJoinRepository tripJoinRepository;

    @Transactional
    public DiaryDto createDiary(Long tripId, User currentUser, DiaryCreateRequestDto diaryCreateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        TripJoin tripJoin = tripJoinRepository.findByUserAndTrip(currentUser, trip)
                .orElseThrow(() -> new TripJoinException(TripJoinErrorCode.TRIP_JOIN_NOT_FOUND));

        Diary diary = Diary.createDiary(
                diaryCreateRequestDto.getTitle(),
                diaryCreateRequestDto.getContent(),
                diaryCreateRequestDto.getImageUrls(),
                diaryCreateRequestDto.getDate(),
                diaryCreateRequestDto.getEmotion(),
                trip,
                tripJoin
        );

        diaryRepository.save(diary);

        return diaryMapper.toDto(diary);
    }

    public CursorPageResponseDto<DiaryDto> getDiaries(Long tripId, String keyword, String orderBy, String direction, String cursor, Long after, int limit) {
        List<Diary> diaries = diaryRepository.findAllByCursor(tripId, keyword, orderBy, direction, cursor, after, limit + 1);

        boolean hasNext = diaries.size() > limit;

        List<Diary> pagedDiaries = hasNext ? diaries.subList(0, limit) : diaries;

        List<DiaryDto> diaryDtos = pagedDiaries.stream()
                .map(diaryMapper::toDto)
                .toList();

        String nextCursor = null;
        Long nextAfter = null;

        if (hasNext) {
            Diary lastDiary = pagedDiaries.getLast();

            if (orderBy.equals("createdAt")) {
                nextCursor = lastDiary.getCreatedAt().toString();
            }

            nextAfter = lastDiary.getId();
        }

        return CursorPageResponseDto.<DiaryDto>builder()
                .content(diaryDtos)
                .nextCursor(nextCursor)
                .nextAfter(nextAfter)
                .size(diaryDtos.size())
                .hasNext(hasNext)
                .build();
    }

    public DiaryDto getDiary(Long tripId, Long diaryId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException(DiaryErrorCode.DIARY_NOT_FOUND));

        validateDiaryBelongsToTrip(diary, trip);

        return diaryMapper.toDto(diary);
    }

    @Transactional
    public DiaryDto updateDiary(Long tripId, Long diaryId, DiaryUpdateRequestDto diaryUpdateRequestDto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException(DiaryErrorCode.DIARY_NOT_FOUND));

        validateDiaryBelongsToTrip(diary, trip);

        diary.update(
                diaryUpdateRequestDto.getTitle(),
                diaryUpdateRequestDto.getContent(),
                diaryUpdateRequestDto.getImageUrls(),
                diaryUpdateRequestDto.getDate(),
                diaryUpdateRequestDto.getEmotion()
        );

        return diaryMapper.toDto(diary);
    }

    @Transactional
    public void deleteDiary(Long tripId, Long diaryId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(TripErrorCode.TRIP_NOT_FOUND));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException(DiaryErrorCode.DIARY_NOT_FOUND));

        validateDiaryBelongsToTrip(diary, trip);

        diaryRepository.delete(diary);
    }

    private void validateDiaryBelongsToTrip(Diary diary, Trip trip) {
        if (!diary.getTrip().equals(trip)) {
            throw new DiaryException(DiaryErrorCode.DIARY_NOT_BELONG_TO_TRIP);
        }
    }
}

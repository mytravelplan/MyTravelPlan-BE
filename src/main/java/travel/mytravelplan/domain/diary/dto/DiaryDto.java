package travel.mytravelplan.domain.diary.dto;

import lombok.Builder;
import lombok.Getter;
import travel.mytravelplan.domain.diary.enums.Emotion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class DiaryDto {
    private Long id;
    private String title;
    private LocalDate date;
    private Emotion emotion;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private DiaryDto(Long id, String title, LocalDate date, Emotion emotion, String content, List<String> imageUrls, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.emotion = emotion;
        this.content = content;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

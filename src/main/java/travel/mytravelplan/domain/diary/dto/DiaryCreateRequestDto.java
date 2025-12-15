package travel.mytravelplan.domain.diary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.diary.enums.Emotion;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class DiaryCreateRequestDto {
    private String title;
    private String content;
    private List<String> imageUrls;
    private LocalDate date;
    private Emotion emotion;

    @Builder
    private DiaryCreateRequestDto(String title, String content, List<String> imageUrls, LocalDate date, Emotion emotion) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
        this.date = date;
        this.emotion = emotion;
    }
}

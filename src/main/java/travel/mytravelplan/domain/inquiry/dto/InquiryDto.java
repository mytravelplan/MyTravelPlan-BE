package travel.mytravelplan.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryDto {
    private Long id;
    private String title;
    private String content;
    private boolean answered;
    private boolean secret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private InquiryDto(Long id, String title, String content, boolean answered, boolean secret,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.answered = answered;
        this.secret = secret;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

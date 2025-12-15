package travel.mytravelplan.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryCreateRequestDto {
    private String title;
    private String content;
    private boolean secret;

    @Builder
    private InquiryCreateRequestDto(String title, String content, boolean secret) {
        this.title = title;
        this.content = content;
        this.secret = secret;
    }
}

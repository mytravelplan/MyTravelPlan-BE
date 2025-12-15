package travel.mytravelplan.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryUpdateRequestDto {
    private String title;
    private String content;
    private boolean secret;

    @Builder
    private InquiryUpdateRequestDto(String title, String content, boolean secret) {
        this.title = title;
        this.content = content;
        this.secret = secret;
    }
}

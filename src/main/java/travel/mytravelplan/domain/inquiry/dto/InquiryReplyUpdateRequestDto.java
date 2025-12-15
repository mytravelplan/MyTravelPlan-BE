package travel.mytravelplan.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryReplyUpdateRequestDto {
    private String content;

    @Builder
    private InquiryReplyUpdateRequestDto(String content) {
        this.content = content;
    }
}

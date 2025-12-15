package travel.mytravelplan.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryReplyCreateRequestDto {
    private String content;

    @Builder
    private InquiryReplyCreateRequestDto(String content) {
        this.content = content;
    }
}

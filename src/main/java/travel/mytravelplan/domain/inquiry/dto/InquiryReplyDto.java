package travel.mytravelplan.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InquiryReplyDto {
    private Long id;
    private String content;

    @Builder
    private InquiryReplyDto(Long id, String content) {
        this.id = id;
        this.content = content;
    }
}

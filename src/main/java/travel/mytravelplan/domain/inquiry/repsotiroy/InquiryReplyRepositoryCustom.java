package travel.mytravelplan.domain.inquiry.repsotiroy;

import travel.mytravelplan.domain.inquiry.entity.InquiryReply;

import java.util.List;

public interface InquiryReplyRepositoryCustom {
    List<InquiryReply> findAllByCursor(Long inquiryId, String keyword, String orderBy, String direction, String cursor, Long after, int limit);
}

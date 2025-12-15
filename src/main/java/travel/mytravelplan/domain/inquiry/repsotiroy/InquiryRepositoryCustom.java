package travel.mytravelplan.domain.inquiry.repsotiroy;

import travel.mytravelplan.domain.inquiry.entity.Inquiry;

import java.util.List;

public interface InquiryRepositoryCustom {
    List<Inquiry> findAllByCursor(Long productId, String keyword, boolean secretOnly, boolean answerOnly, String orderBy, String direction, String cursor, Long after, int limit);
}

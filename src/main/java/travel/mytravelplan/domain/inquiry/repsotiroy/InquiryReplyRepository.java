package travel.mytravelplan.domain.inquiry.repsotiroy;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;
import travel.mytravelplan.domain.inquiry.entity.InquiryReply;

import java.util.List;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long>, InquiryReplyRepositoryCustom {
}

package travel.mytravelplan.domain.inquiry.repsotiroy;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, Long>, InquiryRepositoryCustom {
}

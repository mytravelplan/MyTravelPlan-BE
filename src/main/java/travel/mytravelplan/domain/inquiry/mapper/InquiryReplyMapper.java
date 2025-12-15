package travel.mytravelplan.domain.inquiry.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.inquiry.dto.InquiryReplyDto;
import travel.mytravelplan.domain.inquiry.entity.InquiryReply;
import travel.mytravelplan.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface InquiryReplyMapper {
    @Mapping(target = "id", source = "inquiryReply.id")
    InquiryReplyDto toDto(InquiryReply inquiryReply, User currentUser);
}

package travel.mytravelplan.domain.inquiry.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.inquiry.dto.InquiryDto;
import travel.mytravelplan.domain.inquiry.entity.Inquiry;
import travel.mytravelplan.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface InquiryMapper {

    @Mapping(target = "id", source = "inquiry.id")
    @Mapping(target = "title", source = "inquiry.title")
    @Mapping(target = "content", expression = "java(inquiry.getUser().getId().equals(user.getId()) ? inquiry.getContent() : \"비밀 문의글 입니다.\")")
    @Mapping(target = "answered", expression = "java(!inquiry.getInquiryReplies().isEmpty())")
    @Mapping(target = "secret", source = "inquiry.secret")
    @Mapping(target = "createdAt", source = "inquiry.createdAt")
    @Mapping(target = "updatedAt", source = "inquiry.updatedAt")
    InquiryDto toDto(Inquiry inquiry, User user);
}

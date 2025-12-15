package travel.mytravelplan.domain.question.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.question.dto.QuestionDto;
import travel.mytravelplan.domain.question.entity.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    @Mapping(target = "id", source = "question.id")
    @Mapping(target = "front", expression = "java(question.getCard().getFront())")
    @Mapping(target = "back", expression = "java(question.getCard().getBack())")
    QuestionDto toDto(Question question);
}

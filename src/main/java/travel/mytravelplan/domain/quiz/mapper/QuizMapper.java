package travel.mytravelplan.domain.quiz.mapper;

import org.mapstruct.Mapper;
import travel.mytravelplan.domain.quiz.dto.QuizDto;
import travel.mytravelplan.domain.quiz.entity.Quiz;

@Mapper(componentModel = "spring")
public interface QuizMapper {
    QuizDto toDto(Quiz quiz);
}

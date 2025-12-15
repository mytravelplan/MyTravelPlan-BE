package travel.mytravelplan.domain.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.answer.dto.AnswerRequestDto;
import travel.mytravelplan.domain.quiz.dto.QuizCreateRequestDto;
import travel.mytravelplan.domain.quiz.dto.QuizDto;
import travel.mytravelplan.domain.quiz.dto.QuizResultDto;
import travel.mytravelplan.domain.quiz.enums.QuizType;
import travel.mytravelplan.domain.quiz.service.QuizService;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.global.common.annotaion.LoginUser;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    // 퀴즈 시작
    @PostMapping("/start")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuizDto>> createQuiz(@LoginUser User currentUser, @RequestBody @Validated QuizCreateRequestDto quizCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(quizService.startQuiz(currentUser, quizCreateRequestDto)));
    }

    // 퀴즈 종료
    @PostMapping("/{quizId}/finish")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#quizId, 'Quiz', 'quiz:finish')")
    public ResponseEntity<ApiResponse<QuizDto>> finishQuiz(@PathVariable Long quizId, @RequestBody @Validated List<AnswerRequestDto> answerRequestDtos) {
        return ResponseEntity.ok(ApiResponse.success(quizService.finishQuiz(quizId, answerRequestDtos)));
    }

    // 퀴즈 결과 조회
    @GetMapping("/{quizId}/result")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#quizId, 'Quiz', 'quiz:result:read')")
    public ResponseEntity<ApiResponse<QuizResultDto>> getQuizResult(@PathVariable Long quizId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizResult(quizId)));
    }

    // 퀴즈 결과 목록 조회
    @GetMapping("/results")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<QuizResultDto>>> getQuizResults(
            @LoginUser User currentUser,
            @RequestParam(required = false) QuizType quizType,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizResults(currentUser.getUsername(), quizType, orderBy, direction, cursor, after, limit)));
    }

    // 퀴즈 결과 삭제
    @DeleteMapping("/{quizId}/result")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#quizId, 'Quiz', 'quiz:result:delete')")
    public ResponseEntity<Void> deleteQuizResult(@PathVariable Long quizId) {
        quizService.deleteQuizResult(quizId);
        return ResponseEntity.noContent().build();
    }
}

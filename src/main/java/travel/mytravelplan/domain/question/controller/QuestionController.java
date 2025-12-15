package travel.mytravelplan.domain.question.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import travel.mytravelplan.domain.question.dto.QuestionDto;
import travel.mytravelplan.domain.question.service.QuestionService;
import travel.mytravelplan.global.common.response.ApiResponse;
import travel.mytravelplan.global.common.response.CursorPageResponseDto;

@RestController
@RequestMapping("/api/quizzes/{quizId}/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    // 퀴즈 질문 목록 조회
    @GetMapping
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and hasPermission(#quizId, 'Quiz', 'quiz:question:read')")
    public ResponseEntity<ApiResponse<CursorPageResponseDto<QuestionDto>>> getQuestions(
            @PathVariable Long quizId,
            @RequestParam(required = false) boolean shuffle,
            @RequestParam(defaultValue = "createdAt", required = false) String orderBy,
            @RequestParam(defaultValue = "ASC", required = false) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10", required = false) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(questionService.getQuestions(quizId, shuffle, orderBy, direction, cursor, after, limit)));
    }
}

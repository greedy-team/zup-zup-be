package com.greedy.zupzup.quiz.presentation;

import com.greedy.zupzup.quiz.application.QuizService;
import com.greedy.zupzup.quiz.application.dto.QuizDto;
import com.greedy.zupzup.quiz.presentation.dto.QuizzesResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/api/lost-items/{lostItemId}/quizzes")
    public ResponseEntity<QuizzesResponse> generateLostItemQuizzes(
            @PathVariable Long lostItemId,
            Long memberId) {
        List<QuizDto> quizDtos = quizService.generateLostItemQuizzes(lostItemId, memberId);
        return ResponseEntity.ok(QuizzesResponse.from(quizDtos));
    }
}

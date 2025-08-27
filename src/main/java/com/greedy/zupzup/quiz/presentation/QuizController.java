package com.greedy.zupzup.quiz.presentation;

import com.greedy.zupzup.auth.presentation.annotation.MemberAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.quiz.application.QuizGenerationService;
import com.greedy.zupzup.quiz.application.QuizSubmissionService;
import com.greedy.zupzup.quiz.application.dto.QuizDto;
import com.greedy.zupzup.quiz.application.dto.QuizResultDto;
import com.greedy.zupzup.quiz.presentation.dto.QuizSubmissionRequest;
import com.greedy.zupzup.quiz.presentation.dto.QuizSubmissionResponse;
import com.greedy.zupzup.quiz.presentation.dto.QuizzesResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lost-items/{lostItemId}/quizzes")
@RequiredArgsConstructor
public class QuizController implements QuizControllerDocs{

    private final QuizGenerationService quizGenerationService;
    private final QuizSubmissionService quizSubmissionService;

    @Override
    @GetMapping
    public ResponseEntity<QuizzesResponse> getLostItemQuizzes(@PathVariable Long lostItemId, @MemberAuth LoginMember loginMember) {
        List<QuizDto> quizDtos = quizGenerationService.getLostItemQuizzes(lostItemId, loginMember.memberId());
        return ResponseEntity.ok(QuizzesResponse.from(quizDtos));
    }

    @Override
    @PostMapping
    public ResponseEntity<QuizSubmissionResponse> submitQuizAnswers(@PathVariable Long lostItemId, @MemberAuth LoginMember loginMember,
            @Valid @RequestBody QuizSubmissionRequest submissionRequest) {
        QuizResultDto resultDto = quizSubmissionService.submitQuizAnswers(lostItemId, loginMember.memberId(), submissionRequest.toCommands());
        return ResponseEntity.ok(QuizSubmissionResponse.from(resultDto));
    }
}

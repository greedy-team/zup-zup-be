package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.AnswerCommand;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

public record QuizSubmissionRequest(
        @NotEmpty(message = "퀴즈 답변을 하나 이상 제출해야 합니다.")
        List<AnswerRequest> answers
) {

    public List<AnswerCommand> toCommands() {
        return answers.stream()
                .map(AnswerCommand::from)
                .collect(Collectors.toList());
    }
}

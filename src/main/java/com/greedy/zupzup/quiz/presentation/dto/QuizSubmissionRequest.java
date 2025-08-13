package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.AnswerCommand;
import java.util.List;
import java.util.stream.Collectors;

public record QuizSubmissionRequest(
        List<AnswerRequest> answers
) {

    public List<AnswerCommand> toCommands() {
        return answers.stream()
                .map(AnswerCommand::from)
                .collect(Collectors.toList());
    }
}

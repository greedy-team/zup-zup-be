package com.greedy.zupzup.quiz.application.dto;

import java.util.List;

public record QuizDto(
        Long featureId,
        String question,
        List<OptionDto> options
) {

}

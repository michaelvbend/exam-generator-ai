package com.examgenerator.examgenerator.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record Exam(List<Question> questionList) {
}

package com.examgenerator.examgenerator.model.response;

import com.examgenerator.examgenerator.domain.Exam;
import lombok.Builder;

@Builder
public record ExamResponse(Exam exam) {
}

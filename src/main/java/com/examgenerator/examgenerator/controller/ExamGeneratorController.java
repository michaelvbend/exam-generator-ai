package com.examgenerator.examgenerator.controller;

import com.examgenerator.examgenerator.model.request.ExamRequest;
import com.examgenerator.examgenerator.model.response.ExamResponse;
import com.examgenerator.examgenerator.service.impl.ExamGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
@Slf4j
public class ExamGeneratorController {

    private final ExamGeneratorService examGeneratorService;

    public ExamGeneratorController(ExamGeneratorService examGeneratorService) {
        this.examGeneratorService = examGeneratorService;
    }

    @PostMapping()
    public ExamResponse generateExam(@RequestBody ExamRequest examRequest) {
        return examGeneratorService.generateExam(examRequest);
    }

}

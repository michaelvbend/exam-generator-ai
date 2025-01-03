package com.examgenerator.examgenerator.controller;

import com.examgenerator.examgenerator.model.response.ExamResponse;
import com.examgenerator.examgenerator.service.impl.ExamGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/exams")
@Slf4j
public class ExamGeneratorController {

    private final ExamGeneratorService examGeneratorService;

    public ExamGeneratorController(ExamGeneratorService examGeneratorService) {
        this.examGeneratorService = examGeneratorService;
    }

    @PostMapping("/generate")
    public ExamResponse generateExam(@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "emphasis") String emphasisQuery) {
        return examGeneratorService.generateExam(file, emphasisQuery);
    }

}

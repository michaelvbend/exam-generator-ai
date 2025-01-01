package com.examgenerator.examgenerator.controller;

import com.examgenerator.examgenerator.domain.Exam;
import com.examgenerator.examgenerator.service.impl.ExamGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Slf4j
public class ExamGeneratorController {

    private final ExamGeneratorService examGeneratorService;

    @PostMapping("/generate")
    public Exam generateExam(@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "topic") String topic) {
        return examGeneratorService.generateExam(file, topic);
    }


}

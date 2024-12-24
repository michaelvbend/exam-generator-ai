package com.examgenerator.examgenerator.controller;

import com.examgenerator.examgenerator.domain.Question;
import com.examgenerator.examgenerator.service.ExamGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamGeneratorController {

    private final ExamGeneratorService examGeneratorService;

    @GetMapping("/generate")
    public Question generate(@RequestParam(value = "topic") String topic) {
       return examGeneratorService.generateQuestion(topic);
    }


}

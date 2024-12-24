package com.examgenerator.examgenerator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamGeneratorController {

    private final OllamaChatModel chatModel;

    @GetMapping("/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Write 10 questions about Vitesse Arnhem") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }


}

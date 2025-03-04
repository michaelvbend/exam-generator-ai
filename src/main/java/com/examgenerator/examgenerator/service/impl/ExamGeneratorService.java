package com.examgenerator.examgenerator.service.impl;

import com.examgenerator.examgenerator.domain.Exam;
import com.examgenerator.examgenerator.domain.Question;
import com.examgenerator.examgenerator.exception.QuestionGenerationException;
import com.examgenerator.examgenerator.model.request.ExamRequest;
import com.examgenerator.examgenerator.model.response.ExamResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for generating exams with AI-powered questions
 * based on provided topics and context.
 */
@Service
@Slf4j
public class ExamGeneratorService {

    private static final int MAX_AMOUNT_OF_QUESTIONS = 5;

    private final ChatClient chatClient;
    private final BeanOutputConverter<Question> outputConverter;
    private final Resource questionPromptTemplate;

    public ExamGeneratorService(ChatClient.Builder builder, @Value("classpath:/prompts/generate-single-question.st") Resource questionPromptTemplate) {
        this.chatClient = builder.build();
        this.outputConverter = new BeanOutputConverter<>(Question.class);
        this.questionPromptTemplate = questionPromptTemplate;
        log.info("ExamGeneratorService initialized");
    }

    public ExamResponse generateExam(ExamRequest examRequest) {
        log.info("Generating exam for topic: {}", examRequest.topic());
        String format = outputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate(questionPromptTemplate);

        List<Question> questions = new ArrayList<>();

        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("format", format);
        promptParams.put("context", examRequest.prompt());
        promptParams.put("topic", examRequest.topic());
        int generatedQuestions = 0;
        while (generatedQuestions < MAX_AMOUNT_OF_QUESTIONS) {
            log.debug("Generating question {} of {}", generatedQuestions+1, MAX_AMOUNT_OF_QUESTIONS);
            Question question = generateQuestion(promptTemplate.create(promptParams));
            if (isValidQuestion(question)) {
                questions.add(question);
                generatedQuestions++;
            }
        }

        Exam exam = Exam.builder().questionList(questions).build();
        log.info("Successfully generated exam with {} questions", questions.size());
        return ExamResponse.builder().exam(exam).build();
    }

    private Question generateQuestion(Prompt prompt) {
        try {
            String response = chatClient.prompt(prompt).call().content();
            if (response == null) {
                log.error("AI returned null response");
                throw new QuestionGenerationException("AI response is null");
            }
            return outputConverter.convert(response);
        } catch (Exception e) {
            log.error("Failed to generate question", e);
            throw new QuestionGenerationException("Could not generate question: " + e.getMessage(), e);
        }
    }

    private boolean isValidQuestion(Question question) {
        return question != null &&
                !question.question().isBlank() &&
                !question.answer().isBlank() &&
                question.options() != null &&
                question.options().size() == 3;
    }
}
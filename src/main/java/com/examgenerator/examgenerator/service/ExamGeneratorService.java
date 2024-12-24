package com.examgenerator.examgenerator.service;

import com.examgenerator.examgenerator.domain.Exam;
import com.examgenerator.examgenerator.domain.Question;
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

@Service
@Slf4j
public class ExamGeneratorService {
    private static final int MAX_AMOUNT_OF_QUESTIONS = 10;
    private final ChatClient chatClient;
    private final BeanOutputConverter<Question> outputConverter = new BeanOutputConverter<>(Question.class);

    @Value("classpath:/prompts/single-question.st")
    private Resource singleQuestionPrompt;

    public ExamGeneratorService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public Exam generateExam(String topic) {
        List<Question> listOfQuestions = new ArrayList<>();
        while (listOfQuestions.size() < MAX_AMOUNT_OF_QUESTIONS) {
            try {
                listOfQuestions.add(generateQuestion(topic));
            } catch (Exception exception) {
                log.warn("Could not generate question", exception);
            }
        }
        return Exam.builder().questionList(listOfQuestions).build();
    }

    private Question generateQuestion(String topic) {
        String format = outputConverter.getFormat();

        PromptTemplate promptTemplate = new PromptTemplate(singleQuestionPrompt);
        Map<String, Object> map = new HashMap<>();
        map.put("topic", topic);
        map.put("format", format);

        Prompt prompt = promptTemplate.create(map);
        String response = chatClient.prompt(prompt).call().content();
        if (response != null) {
            return outputConverter.convert(response);
        }
        throw new IllegalStateException("Failed to generate question for topic: " + topic);
    }
}
package com.examgenerator.examgenerator.service.impl;

import com.examgenerator.examgenerator.domain.Exam;
import com.examgenerator.examgenerator.domain.Question;
import com.examgenerator.examgenerator.service.FileService;
import com.fasterxml.jackson.core.io.JsonEOFException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExamGeneratorService {
    private static final int MAX_AMOUNT_OF_QUESTIONS = 3;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final FileService fileService;
    private final BeanOutputConverter<Question> outputConverter = new BeanOutputConverter<>(Question.class);

    @Value("classpath:/prompts/single-question.st")
    private Resource singleQuestionPrompt;

    public ExamGeneratorService(ChatClient.Builder builder, VectorStore vectorStore, FileService fileService) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.fileService = fileService;
    }

    public Exam generateExam(MultipartFile file, String topic) {
        Document resource = fileService.sanitizeFile(file);
        Prompt prompt = generatePrompt(resource, topic);
        List<Question> listOfQuestions = new ArrayList<>();
        while (listOfQuestions.size() < MAX_AMOUNT_OF_QUESTIONS) {
            try {
                listOfQuestions.add(generateQuestion(prompt));
            } catch (Exception exception) {
                log.warn("Could not generate question", exception);
            }
        }
        return Exam.builder().questionList(listOfQuestions).build();
    }

    private Prompt generatePrompt(Document resource, String topic) {
        String format = outputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate(singleQuestionPrompt);
        Map<String, Object> map = new HashMap<>();

        map.put("context", this.vectorStore.similaritySearch(resource.getContent()));
        map.put("topic", topic);
        map.put("format", format);

        return promptTemplate.create(map);
    }

    private Question generateQuestion(Prompt prompt) {
        try {
            String response = chatClient.prompt(prompt).call().content();
            if (response == null) {
                throw new RuntimeException("Could not generate question");
            }
            return outputConverter.convert(response);
        } catch (Exception e) {
            throw new RuntimeException("Could not generate question");
        }
    }

}
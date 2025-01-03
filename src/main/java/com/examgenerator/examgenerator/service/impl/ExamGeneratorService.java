package com.examgenerator.examgenerator.service.impl;

import com.examgenerator.examgenerator.domain.Exam;
import com.examgenerator.examgenerator.domain.Question;
import com.examgenerator.examgenerator.model.response.ExamResponse;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamGeneratorService {
    private static final int MAX_AMOUNT_OF_QUESTIONS = 3;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final DocumentLoadingService documentLoadingService;
    private final FileService fileService;
    private final BeanOutputConverter<Question> outputConverter = new BeanOutputConverter<>(Question.class);

    @Value("classpath:/prompts/single-question.st")
    private Resource singleQuestionPrompt;

    public ExamGeneratorService(ChatClient.Builder builder, VectorStore vectorStore, FileService fileService, DocumentLoadingService documentLoadingService) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.fileService = fileService;
        this.documentLoadingService = documentLoadingService;

    }

    public ExamResponse generateExam(MultipartFile file, String emphasis) {
        List<String> textList = fileService.sanitizeFile(file);
        String format = outputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate(singleQuestionPrompt);

        List<Question> listOfQuestions = new ArrayList<>();
        for(String str: textList) {
            try {
                Map<String, Object> promptParams = new HashMap<>();
                promptParams.put("format", format);
                promptParams.put("context", str);
                listOfQuestions.add(generateQuestion(promptTemplate.create(promptParams)));

            } catch (Exception exception) {
                log.warn("Could not generate question", exception);
            }

        }
        return ExamResponse.builder().exam(Exam.builder().questionList(listOfQuestions).build()).build();
    }

//    private List<Prompt> generatePrompts(Document document) {
//
//    }

    private List<Prompt> generatePrompt(String emphasis) {
        String format = outputConverter.getFormat();
        List<Prompt> promptList = new ArrayList<>();
        PromptTemplate promptTemplate = new PromptTemplate(singleQuestionPrompt);
        List<String> contextList = this.vectorStore.similaritySearch(emphasis)
                .stream()
                .map(Document::getContent)
                .toList();



        for (String context: contextList) {
            Map<String, Object> promptParams = new HashMap<>();
            promptParams.put("format", format);
            promptParams.put("context", context);
            promptList.add(promptTemplate.create(promptParams));
        }
        return promptList;
    }

    private Question generateQuestion(Prompt prompt) {
        try {
            log.info("Generating question for prompt: {}", prompt);
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
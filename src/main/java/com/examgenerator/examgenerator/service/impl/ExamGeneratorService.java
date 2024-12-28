package com.examgenerator.examgenerator.service;

import com.examgenerator.examgenerator.domain.Exam;
import com.examgenerator.examgenerator.domain.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamGeneratorService {
    private static final int MAX_AMOUNT_OF_QUESTIONS = 10;
    private final ChatClient chatClient;
    private final BeanOutputConverter<Question> outputConverter = new BeanOutputConverter<>(Question.class);

    @Value("classpath:/prompts/single-question.st")
    private Resource singleQuestionPrompt;

    @Value("classpath:/docs/dummy.pdf")
    private Resource dummyPdf;

    public ExamGeneratorService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public Exam generateExam(String topic) {
        Prompt prompt = generatePrompt(topic);

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

    private Prompt generatePrompt(String topic) {
        String format = outputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate(singleQuestionPrompt);
        Map<String, Object> map = new HashMap<>();

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().build())
                .build();
        var pdfReader = new PagePdfDocumentReader(dummyPdf, config);
        String context = pdfReader.get().stream().map(Document::getContent).collect(Collectors.joining());
        map.put("context", context);
        map.put("format", format);

        return promptTemplate.create(map);
    }

    private Question generateQuestion(Prompt prompt) {
        String response = chatClient.prompt(prompt).call().content();
        return outputConverter.convert(response);
    }
}
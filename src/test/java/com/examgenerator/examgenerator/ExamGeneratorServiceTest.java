package com.examgenerator.examgenerator;

import com.examgenerator.examgenerator.domain.Question;
import com.examgenerator.examgenerator.model.request.ExamRequest;
import com.examgenerator.examgenerator.model.response.ExamResponse;
import com.examgenerator.examgenerator.service.impl.ExamGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExamGeneratorServiceTest {

    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private BeanOutputConverter<Question> outputConverter;
    @Mock
    private Resource questionPromptTemplate;

    private ExamGeneratorService examGeneratorService;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(questionPromptTemplate.getInputStream()).thenReturn(new ByteArrayInputStream("Test template".getBytes()));
        when(chatClientBuilder.build()).thenReturn(chatClient);
        examGeneratorService = new ExamGeneratorService(chatClientBuilder, questionPromptTemplate);
    }

    @Test
    void generateExamSuccessfully() {
        ExamRequest examRequest = new ExamRequest("Math", "Solve the following problems");
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        String jsonResponse = "{\"question\":\"What is 2+2?\",\"answer\":\"4\",\"options\":[\"3\",\"4\",\"5\"]}";

        // Remove chatClient.getBeanOutputConverter() as it's not used
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(jsonResponse);

        ExamResponse examResponse = examGeneratorService.generateExam(examRequest);

        assertNotNull(examResponse);
        assertEquals(5, examResponse.exam().questionList().size());
    }
//
//    @Test
//    void generateExamWithInvalidQuestion() {
//        ExamRequest examRequest = new ExamRequest("Math", "Solve the following problems");
//        when(outputConverter.getFormat()).thenReturn("json");
//        when(chatClient.prompt(any(Prompt.class)).call().content()).thenReturn("{\"question\":\"\",\"answer\":\"\",\"options\":[]}");
//
//        assertThrows(QuestionGenerationException.class, () -> examGeneratorService.generateExam(examRequest));
//    }
//
//    @Test
//    void generateExamWithNullResponse() {
//        ExamRequest examRequest = new ExamRequest("Math", "Solve the following problems");
//        when(outputConverter.getFormat()).thenReturn("json");
//        when(chatClient.prompt(any(Prompt.class)).call().content()).thenReturn(null);
//
//        assertThrows(QuestionGenerationException.class, () -> examGeneratorService.generateExam(examRequest));
//    }
//
//    @Test
//    void generateExamWithException() {
//        ExamRequest examRequest = new ExamRequest("Math", "Solve the following problems");
//        when(outputConverter.getFormat()).thenReturn("json");
//        when(chatClient.prompt(any(Prompt.class)).call()).thenThrow(new RuntimeException("Chat client error"));
//
//        assertThrows(QuestionGenerationException.class, () -> examGeneratorService.generateExam(examRequest));
//    }
}
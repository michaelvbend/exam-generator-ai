package com.examgenerator.examgenerator.service;

import com.examgenerator.examgenerator.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamGeneratorService {

    private final OllamaChatModel chatModel;

    @Value("classpath:/prompts/single-question.st")
    private Resource singleQuestionPrompt;

    public Question generateQuestion(String topic) {
        BeanOutputConverter<Question> outputConverter = new BeanOutputConverter<>(Question.class);
        String format = outputConverter.getFormat();

        PromptTemplate promptTemplate = new PromptTemplate(singleQuestionPrompt);
        Map<String, Object> map = new HashMap<>();
        map.put("topic", topic);
        map.put("format", format);
        Prompt prompt = promptTemplate.create(map);
        String response = this.chatModel.call(prompt).getResult().getOutput().getContent();
        return outputConverter.convert(response);
    }
}

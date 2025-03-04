package com.examgenerator.examgenerator.exception;

public class QuestionGenerationException extends RuntimeException {

    public QuestionGenerationException(String message) {
        super(message);
    }

    public QuestionGenerationException(String message, Exception exception) {
        super(message, exception);
    }
}
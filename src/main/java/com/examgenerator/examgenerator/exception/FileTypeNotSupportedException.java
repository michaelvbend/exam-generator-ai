package com.examgenerator.examgenerator.exception;

public class FileTypeNotSupportedException extends RuntimeException {

    public FileTypeNotSupportedException(String message) {
        super(message);
    }
}
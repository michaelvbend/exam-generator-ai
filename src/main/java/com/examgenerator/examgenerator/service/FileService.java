package com.examgenerator.examgenerator.service;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    Document sanitizeFile(MultipartFile file);
}
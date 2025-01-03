package com.examgenerator.examgenerator.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    List<String> sanitizeFile(MultipartFile file);
}
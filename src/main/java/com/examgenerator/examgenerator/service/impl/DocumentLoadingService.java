package com.examgenerator.examgenerator.service.impl;

import com.examgenerator.examgenerator.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Service
@Slf4j
public class DocumentLoadingService {
    private final FileService fileService;
    private final Random random;
    Map<Integer, Integer> chunkMap = new HashMap<>();

    public DocumentLoadingService(FileService fileService, Random random) {
        this.fileService = fileService;
        this.random = random;
    }



}

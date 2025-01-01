package com.examgenerator.examgenerator.service.impl;

import com.examgenerator.examgenerator.exception.FileTypeNotSupportedException;
import com.examgenerator.examgenerator.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfFileService implements FileService {
    // TODO: LOAD FROM TEXT FILE
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
            "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself",
            "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which",
            "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were",
            "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing",
            "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of",
            "at", "by", "for", "with", "about", "against", "between", "into", "through", "during",
            "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on",
            "off", "over", "under", "again", "further", "then", "once", "here", "there", "when",
            "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other",
            "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very",
            "s", "t", "can", "will", "just", "don", "should", "now", "d", "ll", "m", "o", "re",
            "ve", "y", "ain", "aren", "couldn", "didn", "doesn", "hadn", "hasn", "haven", "isn",
            "ma", "mightn", "mustn", "needn", "shan", "shouldn", "wasn", "weren", "won", "wouldn",
            "one", "etc", "also", "]"
    ));

    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    @Value("classpath:/docs/dummy-pdf.pdf")
    private Resource file;
    public void loadDocumentIntoVectorStore(Document document) {
        TokenTextSplitter textSplitter = new TokenTextSplitter();

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().withNumberOfBottomTextLinesToDelete(0)
                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                        .build())
                .build();

        var pdfReader = new PagePdfDocumentReader(file, config);

        List<Document> splitDocuments = textSplitter.split(List.of(document));
        for (Document doc: splitDocuments) {
            System.out.println("TEXT:::" + doc.getContent());
        }
        System.out.println(splitDocuments.size());
        this.vectorStore.accept(splitDocuments);
    }

    @Override
    public Document sanitizeFile(MultipartFile file) {
        validateFile(file);
        try (PDDocument pdDocument = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String pdfTextStripped = removeStopwordsFromString(pdfTextStripper.getText(pdDocument));
            Document document = Document.builder().withContent(pdfTextStripped).build();
            loadDocumentIntoVectorStore(document);
            return document;
        } catch (IOException e) {
            throw new RuntimeException("Failed to sanitize file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        // TODO: DO MORE THOROUGH VALIDATION IF FILE IS PDF, USE HEAD MAGIC NUMBER
        if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            String contentType = file.getContentType() == null ? "null" : file.getContentType();
            log.error("Invalid file type: {}", contentType);
            throw new FileTypeNotSupportedException("Invalid file type: " + contentType + ". Only PDF files are allowed.");
        }
    }

    private String removeStopwordsFromString(String input) {
        return Arrays.stream(input.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+"))
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.joining(" "));
    }
}

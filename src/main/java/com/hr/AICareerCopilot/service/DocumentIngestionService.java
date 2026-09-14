package com.hr.AICareerCopilot.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final ResumeContextService resumeContextService;

    public DocumentIngestionService(
            VectorStore vectorStore,
            ResumeContextService resumeContextService) {

        this.vectorStore = vectorStore;
        this.resumeContextService = resumeContextService;
    }

    public String ingestResume(
            MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resume file is empty."
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null ||
                !fileName.toLowerCase().endsWith(".pdf")) {

            throw new IllegalArgumentException(
                    "Only PDF resumes are supported."
            );
        }

        String resumeId =
                resumeContextService.createResumeId();

        InputStreamResource resource =
                new InputStreamResource(
                        file.getInputStream()
                );

        PagePdfDocumentReader pdfReader =
                new PagePdfDocumentReader(resource);

        List<Document> documents =
                pdfReader.get();

        if (documents == null ||
                documents.isEmpty()) {

            throw new IllegalStateException(
                    "Could not extract text from the resume."
            );
        }

        /*
         * Build COMPLETE extracted resume text
         * before chunking.
         */
        String fullResumeText =
                documents.stream()
                        .map(Document::getText)
                        .filter(
                                text ->
                                        text != null &&
                                                !text.isBlank()
                        )
                        .collect(
                                Collectors.joining(
                                        "\n\n"
                                )
                        )
                        .trim();

        if (fullResumeText.isBlank()) {
            throw new IllegalStateException(
                    "Resume text could not be extracted."
            );
        }

        /*
         * Save the complete resume text for the analyzer.
         */
        resumeContextService.saveResumeText(
                resumeId,
                fullResumeText
        );

        /*
         * Chunk the same resume for PGVector / RAG.
         */
        TokenTextSplitter splitter =
                TokenTextSplitter.builder()
                        .withChunkSize(800)
                        .withMinChunkSizeChars(350)
                        .withMinChunkLengthToEmbed(5)
                        .withMaxNumChunks(10000)
                        .build();

        List<Document> chunks =
                splitter.apply(documents);

        chunks =
                resumeContextService.addResumeMetadata(
                        chunks,
                        resumeId,
                        fileName
                );

        vectorStore.add(chunks);

        return resumeId;
    }
}
package com.hr.AICareerCopilot.service;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResumeContextService {

    private final Map<String, String> resumeTexts =
            new ConcurrentHashMap<>();

    public String createResumeId() {
        return UUID.randomUUID().toString();
    }

    public List<Document> addResumeMetadata(
            List<Document> documents,
            String resumeId,
            String fileName) {

        for (Document document : documents) {

            Map<String, Object> metadata =
                    new HashMap<>(document.getMetadata());

            metadata.put("resumeId", resumeId);
            metadata.put("fileName", fileName);

            document.getMetadata().putAll(metadata);
        }

        return documents;
    }

    public void saveResumeText(
            String resumeId,
            String resumeText) {

        if (resumeId == null || resumeId.isBlank()) {
            throw new IllegalArgumentException(
                    "Resume ID is required."
            );
        }

        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException(
                    "Resume text is empty."
            );
        }

        resumeTexts.put(
                resumeId,
                resumeText.trim()
        );
    }

    public String getResumeText(String resumeId) {

        if (resumeId == null || resumeId.isBlank()) {
            return null;
        }

        return resumeTexts.get(resumeId);
    }



    public void clearResumeText(String resumeId) {

        if (resumeId != null && !resumeId.isBlank()) {
            resumeTexts.remove(resumeId);
        }
    }
}
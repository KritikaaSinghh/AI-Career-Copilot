package com.hr.AICareerCopilot.controller;

import com.hr.AICareerCopilot.service.ActiveResumeService;
import com.hr.AICareerCopilot.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final DocumentIngestionService documentIngestionService;
    private final ActiveResumeService activeResumeService;

    public IngestionController(
            DocumentIngestionService documentIngestionService,
            ActiveResumeService activeResumeService) {

        this.documentIngestionService = documentIngestionService;
        this.activeResumeService = activeResumeService;
    }

    @PostMapping("/resume")
    public ResponseEntity<String> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Session-Id") String sessionId) {

        try {

            if (sessionId == null || sessionId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Session ID is required.");
            }

            String resumeId =
                    documentIngestionService.ingestResume(file);

            activeResumeService.setActiveResumeId(
                    sessionId,
                    resumeId
            );

            return ResponseEntity.ok(
                    "Resume uploaded successfully!"
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Failed to process resume: " + e.getMessage());
        }
    }
}
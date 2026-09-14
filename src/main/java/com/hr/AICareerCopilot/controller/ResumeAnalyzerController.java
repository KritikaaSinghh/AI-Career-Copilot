package com.hr.AICareerCopilot.controller;

import com.hr.AICareerCopilot.service.ResumeAnalyzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume")
public class ResumeAnalyzerController {

    private final ResumeAnalyzerService resumeAnalyzerService;

    public ResumeAnalyzerController(
            ResumeAnalyzerService resumeAnalyzerService) {
        this.resumeAnalyzerService =
                resumeAnalyzerService;
    }

    @GetMapping("/analyze")
    public ResponseEntity<String> analyzeResume(
            @RequestHeader("X-Session-Id") String sessionId) {

        try {

            String result =
                    resumeAnalyzerService.analyzeResume(
                            sessionId
                    );

            return ResponseEntity.ok(result);

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Resume analysis failed: " +
                                    e.getMessage()
                    );
        }
    }
}
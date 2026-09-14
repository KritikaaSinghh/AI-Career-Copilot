package com.hr.AICareerCopilot.controller;

import com.hr.AICareerCopilot.dto.InterviewRequest;
import com.hr.AICareerCopilot.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/question")
    public String generateQuestion(
            @Valid @RequestBody InterviewRequest request,
            @RequestHeader("X-Session-Id") String sessionId) {

        return interviewService.generateInterviewQuestion(
                request.getTopic(),
                sessionId
        );
    }

    @PostMapping("/evaluate")
    public String evaluateAnswer(
            @RequestParam String question,
            @RequestBody String answer,
            @RequestHeader("X-Session-Id") String sessionId) {

        return interviewService.evaluateAnswer(

                question,
                answer,
                sessionId
        );
    }
}
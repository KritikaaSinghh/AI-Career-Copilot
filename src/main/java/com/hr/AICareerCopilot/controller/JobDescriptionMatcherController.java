package com.hr.AICareerCopilot.controller;

import com.hr.AICareerCopilot.dto.JobMatchRequest;
import com.hr.AICareerCopilot.service.JobDescriptionMatcherService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job")
public class JobDescriptionMatcherController {

    private final JobDescriptionMatcherService jobDescriptionMatcherService;

    public JobDescriptionMatcherController(
            JobDescriptionMatcherService jobDescriptionMatcherService) {
        this.jobDescriptionMatcherService = jobDescriptionMatcherService;
    }

    @PostMapping("/match")
    public String matchJobDescription(
            @Valid @RequestBody JobMatchRequest request,
            @RequestHeader("X-Session-Id") String sessionId) {

        return jobDescriptionMatcherService.matchJobDescription(
                request.getJobDescription(),
                sessionId
        );
    }
}
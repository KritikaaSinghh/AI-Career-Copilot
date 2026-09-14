package com.hr.AICareerCopilot.controller;

import com.hr.AICareerCopilot.dto.SkillGapRequest;
import com.hr.AICareerCopilot.service.SkillGapAnalyzerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skill-gap")
public class SkillGapAnalyzerController {

    private final SkillGapAnalyzerService skillGapAnalyzerService;

    public SkillGapAnalyzerController(
            SkillGapAnalyzerService skillGapAnalyzerService) {
        this.skillGapAnalyzerService = skillGapAnalyzerService;
    }

    @PostMapping("/analyze")
    public String analyzeSkillGap(
            @Valid @RequestBody SkillGapRequest request,
            @RequestHeader("X-Session-Id") String sessionId) {

        return skillGapAnalyzerService.analyzeSkillGap(
                request.getJobDescription(),
                sessionId
        );
    }
}
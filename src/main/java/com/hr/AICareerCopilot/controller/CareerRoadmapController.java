package com.hr.AICareerCopilot.controller;

import com.hr.AICareerCopilot.dto.CareerRoadmapRequest;
import com.hr.AICareerCopilot.service.CareerRoadmapService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/career")
public class CareerRoadmapController {

    private final CareerRoadmapService careerRoadmapService;

    public CareerRoadmapController(
            CareerRoadmapService careerRoadmapService) {
        this.careerRoadmapService = careerRoadmapService;
    }

    @PostMapping("/roadmap")
    public String generateRoadmap(
            @Valid @RequestBody CareerRoadmapRequest request,
            @RequestHeader("X-Session-Id") String sessionId) {

        return careerRoadmapService.generateRoadmap(
                request.getCareerGoal(),
                sessionId
        );
    }
}
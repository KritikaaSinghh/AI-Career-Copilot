package com.hr.AICareerCopilot.dto;

import jakarta.validation.constraints.NotBlank;

public class CareerRoadmapRequest {

    @NotBlank(message = "Career goal is required")
    private String careerGoal;

    public String getCareerGoal() {
        return careerGoal;
    }

    public void setCareerGoal(String careerGoal) {
        this.careerGoal = careerGoal;
    }
}
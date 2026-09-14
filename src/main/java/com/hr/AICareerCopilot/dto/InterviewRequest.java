package com.hr.AICareerCopilot.dto;

import jakarta.validation.constraints.NotBlank;

public class InterviewRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
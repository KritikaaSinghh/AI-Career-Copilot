package com.hr.AICareerCopilot.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveResumeService {

    /*
     * Stores the active resume for each browser session.
     *
     * sessionId -> resumeId
     */
    private final Map<String, String> activeResumes =
            new ConcurrentHashMap<>();

    public void setActiveResumeId(String sessionId, String resumeId) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID is required.");
        }

        if (resumeId == null || resumeId.isBlank()) {
            throw new IllegalArgumentException("Resume ID is required.");
        }

        activeResumes.put(sessionId, resumeId);
    }

    public String getActiveResumeId(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }

        return activeResumes.get(sessionId);
    }

    public boolean hasActiveResume(String sessionId) {

        String resumeId = getActiveResumeId(sessionId);

        return resumeId != null && !resumeId.isBlank();
    }

    public void clearActiveResume(String sessionId) {

        if (sessionId != null && !sessionId.isBlank()) {
            activeResumes.remove(sessionId);
        }
    }
}
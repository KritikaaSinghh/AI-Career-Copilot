package com.hr.AICareerCopilot.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

@Service
public class InterviewService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3.2:latest";

    private final ActiveResumeService activeResumeService;
    private final ResumeContextService resumeContextService;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    public InterviewService(
            ActiveResumeService activeResumeService,
            ResumeContextService resumeContextService) {

        this.activeResumeService = activeResumeService;
        this.resumeContextService = resumeContextService;
    }

    public String generateInterviewQuestion(
            String topic,
            String sessionId) {

        validateSession(sessionId);

        String resumeId =
                activeResumeService.getActiveResumeId(sessionId);

        if (resumeId == null || resumeId.isBlank()) {
            return "Please upload a resume first.";
        }

        String resumeText =
                resumeContextService.getResumeText(resumeId);

        if (resumeText == null || resumeText.isBlank()) {
            return "Resume content is not available. Please upload your resume again.";
        }

        String cleanTopic =
                topic == null || topic.isBlank()
                        ? "Java"
                        : topic.trim();

        return switch (cleanTopic.toLowerCase(Locale.ROOT)) {

            case "java" ->
                    """
                    Interview Question — Java

                    Explain the difference between an interface and an abstract class in Java.

                    Follow-up:
                    Give one practical example of when you would choose an interface over an abstract class.
                    """;

            case "spring boot", "spring", "springboot" ->
                    """
                    Interview Question — Spring Boot

                    What is dependency injection in Spring Boot, and how does constructor injection work?

                    Follow-up:
                    Why is constructor injection generally preferred over field injection?
                    """;

            case "sql", "database", "dbms" ->
                    """
                    Interview Question — SQL / Database

                    What is the difference between INNER JOIN and LEFT JOIN in SQL?

                    Follow-up:
                    Give an example where LEFT JOIN would be more useful than INNER JOIN.
                    """;

            case "rest", "rest api", "rest apis", "api" ->
                    """
                    Interview Question — REST API

                    What is the difference between GET, POST, PUT and DELETE in a REST API?

                    Follow-up:
                    Which HTTP method would you use to partially update an existing resource?
                    """;

            case "dsa", "data structures", "algorithms" ->
                    """
                    Interview Question — DSA

                    What is the time complexity of searching for an element in an ArrayList?

                    Follow-up:
                    How would the answer change if you used a HashSet instead?
                    """;

            case "spring security", "security", "jwt", "oauth" ->
                    """
                    Interview Question — Security

                    How does authentication differ from authorization in an application?

                    Follow-up:
                    How would JWT-based authentication typically be used in a Spring Boot REST API?
                    """;

            case "docker", "deployment" ->
                    """
                    Interview Question — Docker

                    What problem does Docker solve for a backend application?

                    Follow-up:
                    What is the difference between a Docker image and a Docker container?
                    """;

            default ->
                    """
                    Interview Question — %s

                    Based on your resume, explain one project or technical experience related to %s.
                    Describe your role, the technologies you used, the problem you solved,
                    and one technical challenge you faced.

                    Follow-up:
                    What would you improve in that project if you had more time?
                    """.formatted(cleanTopic, cleanTopic);
        };
    }

    public String evaluateAnswer(
            String question,
            String answer,
            String sessionId) {

        validateSession(sessionId);

        String resumeId =
                activeResumeService.getActiveResumeId(sessionId);

        if (resumeId == null || resumeId.isBlank()) {
            return "Please upload a resume first.";
        }

        String resumeText =
                resumeContextService.getResumeText(resumeId);

        if (resumeText == null || resumeText.isBlank()) {
            return "Resume content is not available. Please upload your resume again.";
        }

        if (question == null || question.isBlank()) {
            return "Interview question is missing.";
        }

        if (answer == null || answer.isBlank()) {
            return """
                    Score: 0/10

                    Feedback:
                    No answer was provided.

                    Improvement:
                    Please provide your answer so it can be evaluated.
                    """;
        }

        String cleanedQuestion = question.trim();
        String cleanedAnswer = answer.trim();

        String resumeForPrompt = limitResume(resumeText, 12000);

        String prompt = """
                You are a strict but fair technical interview evaluator.

                Evaluate the candidate's answer using ONLY the interview question,
                candidate answer, and resume context provided below.

                Do not invent experience that is not present in the resume.
                Do not give a generic evaluation.
                The score must reflect the actual quality and correctness of the answer.

                INTERVIEW QUESTION:
                %s

                CANDIDATE ANSWER:
                %s

                RESUME CONTEXT:
                %s

                Evaluate using these criteria:

                1. Technical correctness
                2. Relevance to the question
                3. Use of concrete technical details
                4. Clarity and structure
                5. Practical understanding
                6. Consistency with the resume

                IMPORTANT:
                - Score from 0 to 10.
                - A detailed and technically correct answer should score higher.
                - A vague or incorrect answer should score lower.
                - Do not penalize the candidate merely because the answer is not long.
                - Mention specific strengths from the actual answer.
                - Mention specific missing or weak points.
                - Give practical improvements.

                Return ONLY this exact format:

                Score: X/10

                Feedback:
                <2-4 sentences specific to the candidate answer>

                Improvement:
                1. <specific improvement>
                2. <specific improvement>
                3. <specific improvement>
                """.formatted(
                cleanedQuestion,
                cleanedAnswer,
                resumeForPrompt
        );

        try {
            return callOllama(prompt);

        } catch (Exception e) {

            return """
                    Interview evaluation could not be completed.

                    Please make sure Ollama is running and try again.
                    """;
        }
    }

    private String callOllama(String prompt)
            throws IOException, InterruptedException {

        String escapedPrompt =
                escapeJson(prompt);

        String jsonBody = """
                {
                  "model": "%s",
                  "prompt": "%s",
                  "stream": false,
                  "options": {
                    "temperature": 0.1,
                    "num_predict": 500,
                    "num_ctx": 8192,
                    "num_thread": 4
                  }
                }
                """.formatted(
                MODEL,
                escapedPrompt
        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(OLLAMA_URL))
                        .header("Content-Type", "application/json")
                        .POST(
                                HttpRequest.BodyPublishers.ofString(jsonBody)
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Ollama returned HTTP " + response.statusCode()
            );
        }

        String body = response.body();

        String generated =
                extractResponse(body);

        if (generated == null || generated.isBlank()) {
            throw new IOException(
                    "Empty response received from Ollama."
            );
        }

        return cleanEvaluation(generated);
    }

    private String extractResponse(String json) {

        String key = "\"response\":\"";

        int start = json.indexOf(key);

        if (start == -1) {
            return null;
        }

        start += key.length();

        StringBuilder result =
                new StringBuilder();

        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {

            char c = json.charAt(i);

            if (escaped) {
                switch (c) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    default -> result.append(c);
                }

                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                break;
            }

            result.append(c);
        }

        return result.toString();
    }

    private String cleanEvaluation(String text) {

        String cleaned =
                text.replace("\r", "")
                        .replaceAll("(?i)^```text\\s*", "")
                        .replaceAll("(?i)^```\\s*", "")
                        .replaceAll("\\s*```$", "")
                        .trim();

        if (!cleaned.toLowerCase(Locale.ROOT)
                .contains("score:")) {

            return """
                    Score: 5/10

                    Feedback:
                    The answer was evaluated, but the model did not return the expected evaluation format.

                    Improvement:
                    1. Give a more structured technical explanation.
                    2. Include concrete implementation details.
                    3. Support the answer with a practical example.
                    """;
        }

        return cleaned;
    }

    private String limitResume(
            String resumeText,
            int maxChars) {

        if (resumeText.length() <= maxChars) {
            return resumeText;
        }

        return resumeText.substring(0, maxChars);
    }

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private void validateSession(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Session ID is required."
            );
        }
    }
}
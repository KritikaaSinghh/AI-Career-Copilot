package com.hr.AICareerCopilot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class JobDescriptionMatcherService {

    private final ActiveResumeService activeResumeService;
    private final ResumeContextService resumeContextService;

    public JobDescriptionMatcherService(
            ActiveResumeService activeResumeService,
            ResumeContextService resumeContextService) {

        this.activeResumeService = activeResumeService;
        this.resumeContextService = resumeContextService;
    }

    public String matchJobDescription(
            String jobDescription,
            String sessionId) {

        if (!activeResumeService.hasActiveResume(sessionId)) {
            return "Please upload a resume first.";
        }

        if (jobDescription == null || jobDescription.isBlank()) {
            return "Please provide a job description.";
        }

        String resumeId =
                activeResumeService.getActiveResumeId(sessionId);

        String resume =
                resumeContextService.getResumeText(resumeId);

        if (resume == null || resume.isBlank()) {
            return "Resume content is not available. Please upload the resume again.";
        }

        List<Requirement> requirements =
                extractRequirements(jobDescription);

        if (requirements.isEmpty()) {
            return "Could not identify clear supported technical requirements from the job description.";
        }

        List<Requirement> matched = new ArrayList<>();
        List<Requirement> missing = new ArrayList<>();

        for (Requirement requirement : requirements) {
            if (isPresent(requirement, resume)) {
                matched.add(requirement);
            } else {
                missing.add(requirement);
            }
        }

        int score =
                (int) Math.round(
                        matched.size() * 100.0 / requirements.size()
                );

        List<String> relevantExperience =
                findRelevantExperience(resume, matched);

        List<String> recommendations =
                buildRecommendations(missing);

        StringBuilder result = new StringBuilder();

        result.append("OVERALL MATCH:\n");
        result.append(
                "Your resume matches "
                        + score
                        + "% of the identified technical job requirements."
        );

        result.append("\n\nMATCHING SKILLS:\n");
        appendRequirements(result, matched);

        result.append("\nMISSING OR WEAK SKILLS:\n");
        appendRequirements(result, missing);

        result.append("\nRELEVANT EXPERIENCE:\n");
        appendStrings(result, relevantExperience);

        result.append("\nMATCH SCORE OUT OF 100:\n");
        result.append(score);

        result.append("\n\nRECOMMENDATIONS:\n");
        appendStrings(result, recommendations);

        return result.toString().trim();
    }

    private List<Requirement> extractRequirements(
            String jobDescription) {

        String jd = normalize(jobDescription);

        List<Requirement> supported =
                supportedRequirements();

        List<Requirement> result =
                new ArrayList<>();

        Set<String> added =
                new LinkedHashSet<>();

        for (Requirement requirement : supported) {

            boolean found = false;

            for (String alias : requirement.aliases()) {

                String normalizedAlias =
                        normalize(alias);

                if (containsPhrase(jd, normalizedAlias)) {
                    found = true;
                    break;
                }
            }

            if (found &&
                    added.add(normalize(requirement.label()))) {

                result.add(requirement);
            }
        }

        return result;
    }

    private List<Requirement> supportedRequirements() {

        List<Requirement> result = new ArrayList<>();

        add(result, "Java", "java");
        add(result, "Python", "python");
        add(result, "JavaScript", "javascript", "js");
        add(result, "TypeScript", "typescript", "ts");
        add(result, "C++", "c++", "cpp");

        add(result, "Spring Boot", "spring boot", "springboot");
        add(result, "Spring Security", "spring security");
        add(result, "Spring Data JPA", "spring data jpa");
        add(result, "Hibernate", "hibernate");
        add(result, "JPA", "jpa");

        add(
                result,
                "REST APIs",
                "rest api",
                "rest apis",
                "restful api",
                "restful apis"
        );

        add(
                result,
                "Backend development",
                "backend development",
                "backend",
                "back end"
        );

        add(result, "Microservices", "microservices");

        add(
                result,
                "Node.js",
                "node.js",
                "nodejs",
                "node js"
        );

        add(
                result,
                "Express.js",
                "express.js",
                "expressjs",
                "express js"
        );

        add(result, "Flask", "flask");

        add(
                result,
                "React.js",
                "react.js",
                "reactjs",
                "react js"
        );

        add(result, "HTML", "html");
        add(result, "CSS", "css");

        add(result, "SQL", "sql");
        add(result, "PostgreSQL", "postgresql");
        add(result, "MySQL", "mysql");

        add(
                result,
                "SQL Server",
                "sql server",
                "mssql",
                "microsoft sql server"
        );

        add(
                result,
                "MongoDB",
                "mongodb",
                "mongo db"
        );

        add(result, "Redis", "redis");

        add(
                result,
                "Data Analysis",
                "data analysis",
                "data analytics",
                "data analyst"
        );

        add(
                result,
                "Data Visualization",
                "data visualization",
                "data visualisation",
                "visualization",
                "visualisation"
        );

        add(result, "Pandas", "pandas");
        add(result, "NumPy", "numpy", "num py");

        add(
                result,
                "Excel",
                "excel",
                "microsoft excel"
        );

        add(
                result,
                "Power BI",
                "power bi",
                "powerbi"
        );

        add(result, "Tableau", "tableau");

        add(
                result,
                "Statistics",
                "statistics",
                "statistical analysis"
        );

        add(result, "Git", "git");
        add(result, "GitHub", "github");
        add(result, "Docker", "docker");
        add(result, "Kubernetes", "kubernetes");
        add(result, "Jenkins", "jenkins");

        add(
                result,
                "AWS",
                "aws",
                "amazon web services"
        );

        add(
                result,
                "Azure",
                "azure",
                "microsoft azure"
        );

        add(
                result,
                "GCP",
                "gcp",
                "google cloud",
                "google cloud platform"
        );

        add(result, "Kafka", "kafka");

        add(
                result,
                "RabbitMQ",
                "rabbitmq",
                "rabbit mq"
        );

        add(
                result,
                "IBM MQ",
                "ibm mq",
                "ibmmq"
        );

        add(result, "JWT", "jwt");

        add(
                result,
                "OAuth",
                "oauth",
                "oauth2",
                "oauth 2"
        );

        add(
                result,
                "Testing",
                "testing",
                "software testing"
        );

        add(result, "JUnit", "junit");

        add(
                result,
                "Object-Oriented Programming (OOP)",
                "object oriented programming",
                "object oriented",
                "oop"
        );

        add(
                result,
                "Data Structures and Algorithms",
                "data structures and algorithms",
                "data structures",
                "algorithms",
                "dsa"
        );

        add(
                result,
                "Problem Solving",
                "problem solving",
                "problem-solving"
        );

        add(result, "Machine Learning", "machine learning");
        add(result, "Generative AI", "generative ai", "gen ai");
        add(
                result,
                "LLMs",
                "llm",
                "llms",
                "large language model",
                "large language models"
        );

        return result;
    }

    private void add(
            List<Requirement> result,
            String label,
            String... aliases) {

        result.add(
                new Requirement(
                        label,
                        List.of(aliases)
                )
        );
    }

    private boolean isPresent(
            Requirement requirement,
            String resume) {

        String normalizedResume =
                normalize(resume);

        for (String alias : requirement.aliases()) {

            if (containsPhrase(
                    normalizedResume,
                    normalize(alias)
            )) {
                return true;
            }
        }

        String key =
                normalize(requirement.label());

        return switch (key) {

            case "spring boot" ->
                    containsPhrase(
                            normalizedResume,
                            "springboot"
                    );

            case "rest apis" ->
                    containsPhrase(
                            normalizedResume,
                            "restful"
                    );

            case "backend development" ->
                    containsPhrase(
                            normalizedResume,
                            "back end"
                    );

            case "object oriented programming oop" ->
                    containsPhrase(
                            normalizedResume,
                            "oop"
                    )
                            || containsPhrase(
                            normalizedResume,
                            "object oriented"
                    );

            case "data structures and algorithms" ->
                    containsPhrase(
                            normalizedResume,
                            "dsa"
                    );

            case "react js" ->
                    containsPhrase(
                            normalizedResume,
                            "reactjs"
                    );

            case "numpy" ->
                    containsPhrase(
                            normalizedResume,
                            "num py"
                    );

            default ->
                    false;
        };
    }

    private List<String> findRelevantExperience(
            String resume,
            List<Requirement> matched) {

        List<String> results =
                new ArrayList<>();

        if (matched.isEmpty()) {
            return results;
        }

        Set<String> keywords =
                new LinkedHashSet<>();

        for (Requirement requirement : matched) {

            for (String alias : requirement.aliases()) {

                String normalized =
                        normalize(alias);

                if (!normalized.isBlank()) {
                    keywords.add(normalized);
                }
            }
        }

        String currentSection = "";

        boolean insideProjects = false;
        boolean insideExperience = false;
        boolean insideSkills = false;

        String[] lines =
                resume.split("\\R");

        for (String rawLine : lines) {

            String line =
                    cleanLine(rawLine);

            if (line.isBlank()) {
                continue;
            }

            if (isContactOrHeaderLine(line)) {
                continue;
            }

            if (isProjectsHeading(line)) {

                insideProjects = true;
                insideExperience = false;
                insideSkills = false;
                currentSection = "";

                continue;
            }

            if (isExperienceHeading(line)) {

                insideProjects = false;
                insideExperience = true;
                insideSkills = false;
                currentSection = "";

                continue;
            }

            if (isSkillsHeading(line)) {

                insideProjects = false;
                insideExperience = false;
                insideSkills = true;
                currentSection = "";

                continue;
            }

            if (isAnotherMajorSection(line)) {

                insideProjects = false;
                insideExperience = false;
                insideSkills = false;
                currentSection = "";

                continue;
            }

            if (insideSkills) {
                continue;
            }

            if (!isEvidenceLine(line)
                    && looksLikeProjectHeading(line)) {

                currentSection = line;
                insideProjects = true;

                continue;
            }

            if (!isEvidenceLine(line)
                    && looksLikeExperienceHeading(line)) {

                currentSection = line;
                insideExperience = true;

                continue;
            }

            if (!isEvidenceLine(line)) {
                continue;
            }

            String normalized =
                    normalize(line);

            if (!containsAnyKeyword(
                    normalized,
                    keywords
            )) {
                continue;
            }

            String cleaned =
                    trimEvidence(line);

            String evidence;

            if (!currentSection.isBlank()
                    && (insideProjects || insideExperience)) {

                evidence =
                        currentSection
                                + " — "
                                + cleaned;
            } else {
                evidence = cleaned;
            }

            if (isContactOrHeaderLine(evidence)) {
                continue;
            }

            addUniqueIgnoreCase(
                    results,
                    evidence
            );

            if (results.size() >= 4) {
                break;
            }
        }

        /*
         * Fallback to genuine action bullets.
         */
        if (results.isEmpty()) {

            for (String rawLine : lines) {

                String line =
                        cleanLine(rawLine);

                if (line.isBlank()
                        || isContactOrHeaderLine(line)
                        || !isEvidenceLine(line)) {
                    continue;
                }

                String normalized =
                        normalize(line);

                if (containsAnyKeyword(
                        normalized,
                        keywords
                )) {

                    addUniqueIgnoreCase(
                            results,
                            trimEvidence(line)
                    );

                    if (results.size() >= 4) {
                        break;
                    }
                }
            }
        }

        return results;
    }

    private boolean containsAnyKeyword(
            String normalizedLine,
            Set<String> keywords) {

        for (String keyword : keywords) {

            if (containsPhrase(
                    normalizedLine,
                    keyword
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean containsPhrase(
            String text,
            String phrase) {

        if (text == null
                || phrase == null
                || text.isBlank()
                || phrase.isBlank()) {

            return false;
        }

        String paddedText =
                " " + text + " ";

        String paddedPhrase =
                " " + phrase + " ";

        return paddedText.contains(
                paddedPhrase
        );
    }

    private List<String> buildRecommendations(
            List<Requirement> missing) {

        List<String> result =
                new ArrayList<>();

        for (Requirement requirement : missing) {

            result.add(
                    "Strengthen evidence for "
                            + requirement.label()
                            + " through a relevant project, measurable result, or clearly documented hands-on work."
            );

            if (result.size() >= 3) {
                break;
            }
        }

        if (result.isEmpty()) {

            result.add(
                    "Your resume covers the identified technical requirements well; focus on stronger measurable outcomes and role-specific evidence."
            );

            result.add(
                    "Keep project and experience bullets concise, technically specific, and easy for recruiters to scan."
            );
        }

        return result;
    }

    private boolean isContactOrHeaderLine(
            String line) {

        String lower =
                line.toLowerCase(Locale.ROOT);

        if (lower.contains("@")) {
            return true;
        }

        if (lower.contains("linkedin")) {
            return true;
        }

        if (lower.contains("github.com")) {
            return true;
        }

        if (lower.contains("portfolio")) {
            return true;
        }

        if (lower.contains("+91")) {
            return true;
        }

        if (line.matches(".*\\d{10,}.*")) {
            return true;
        }

        return lower.matches(
                ".*\\b(new delhi|delhi|gurgaon|gurugram|noida|india|berlin|maharashtra|solapur|jammu|kashmir)\\b.*"
        )
                && !lower.contains("developed")
                && !lower.contains("implemented")
                && !lower.contains("built")
                && !lower.contains("designed")
                && !lower.contains("created")
                && !lower.contains("worked")
                && !lower.contains("managed")
                && !lower.contains("engineered");
    }

    private boolean isProjectsHeading(
            String line) {

        String lower =
                line.toLowerCase(Locale.ROOT)
                        .trim();

        return lower.equals("projects")
                || lower.equals("project")
                || lower.equals("selected projects")
                || lower.equals("academic projects")
                || lower.equals("personal projects");
    }

    private boolean isExperienceHeading(
            String line) {

        String lower =
                line.toLowerCase(Locale.ROOT)
                        .trim();

        return lower.equals("experience")
                || lower.equals("work experience")
                || lower.equals("professional experience")
                || lower.equals("employment")
                || lower.equals("internship")
                || lower.equals("internships");
    }

    private boolean isSkillsHeading(
            String line) {

        String lower =
                line.toLowerCase(Locale.ROOT)
                        .trim();

        return lower.equals("skills")
                || lower.equals("technical skills")
                || lower.equals("key skills")
                || lower.equals("core skills")
                || lower.equals("technology skills")
                || lower.equals("technical competencies");
    }

    private boolean isAnotherMajorSection(
            String line) {

        String lower =
                line.toLowerCase(Locale.ROOT)
                        .trim();

        return lower.equals("education")
                || lower.equals("certifications")
                || lower.equals("achievements")
                || lower.equals("summary")
                || lower.equals("professional summary")
                || lower.equals("profile")
                || lower.equals("objective")
                || lower.equals("additional information")
                || lower.equals("languages")
                || lower.equals("interests")
                || lower.equals("awards");
    }

    private boolean looksLikeProjectHeading(
            String line) {

        String lower =
                line.toLowerCase(Locale.ROOT);

        return lower.contains("url shortener")
                || lower.contains("sorting visualizer")
                || lower.contains("voicegate")
                || lower.contains("sports club website")
                || lower.contains("shop management system")
                || lower.contains("project:")
                || lower.startsWith("project ")
                || lower.startsWith("application:")
                || lower.contains("management system")
                || lower.contains("e-commerce")
                || lower.contains("ecommerce")
                || lower.contains("website");
    }

    private boolean looksLikeExperienceHeading(
            String line) {

        String lower =
                line.toLowerCase(Locale.ROOT);

        return lower.contains("intern")
                || lower.contains("engineer")
                || lower.contains("analyst")
                || lower.contains("developer")
                || lower.contains("associate")
                || lower.contains("manager")
                || lower.contains("captain");
    }

    private boolean isEvidenceLine(
            String line) {

        String lower =
                line.toLowerCase(Locale.ROOT);

        return line.startsWith("•")
                || line.startsWith("-")
                || line.startsWith("*")
                || lower.startsWith("developed ")
                || lower.startsWith("implemented ")
                || lower.startsWith("designed ")
                || lower.startsWith("built ")
                || lower.startsWith("created ")
                || lower.startsWith("managed ")
                || lower.startsWith("used ")
                || lower.startsWith("integrated ")
                || lower.startsWith("worked ")
                || lower.startsWith("configured ")
                || lower.startsWith("deployed ")
                || lower.startsWith("optimized ")
                || lower.startsWith("maintained ")
                || lower.startsWith("automated ")
                || lower.startsWith("tested ")
                || lower.startsWith("engineered ")
                || lower.startsWith("architected ")
                || lower.startsWith("collaborated ")
                || lower.startsWith("tracked ")
                || lower.startsWith("monitored ")
                || lower.startsWith("analyzed ")
                || lower.startsWith("coordinated ")
                || lower.startsWith("improved ");
    }

    private String cleanLine(
            String line) {

        if (line == null) {
            return "";
        }

        return line
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String trimEvidence(
            String line) {

        String cleaned =
                line.replaceFirst(
                        "^[-*•]+\\s*",
                        ""
                ).trim();

        if (cleaned.length() > 220) {

            return cleaned
                    .substring(0, 220)
                    .trim()
                    + "...";
        }

        return cleaned;
    }

    private void appendRequirements(
            StringBuilder out,
            List<Requirement> items) {

        if (items.isEmpty()) {
            out.append("- None identified.\n");
            return;
        }

        for (Requirement item : items) {

            out.append("- ")
                    .append(item.label())
                    .append("\n");
        }
    }

    private void appendStrings(
            StringBuilder out,
            List<String> items) {

        if (items.isEmpty()) {
            out.append("- None identified.\n");
            return;
        }

        for (String item : items) {

            out.append("- ")
                    .append(item)
                    .append("\n");
        }
    }

    private void addUniqueIgnoreCase(
            List<String> items,
            String value) {

        String normalized =
                normalize(value);

        for (String existing : items) {

            if (normalize(existing)
                    .equals(normalized)) {
                return;
            }
        }

        items.add(value);
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace("c/c++", "c cpp")
                .replace("c++", "cpp")
                .replace("node.js", "node js")
                .replace("react.js", "react js")
                .replace("express.js", "express js")
                .replace("restful", "rest")
                .replaceAll("[^a-z0-9+#]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record Requirement(
            String label,
            List<String> aliases
    ) {
    }
}
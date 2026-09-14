package com.hr.AICareerCopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ResumeAnalyzerService {

    private static final String OLLAMA_URL =
            "http://127.0.0.1:11434/api/generate";

    private static final String MODEL =
            "llama3.2:latest";

    private static final int NUM_PREDICT = 700;
    private static final int NUM_CTX = 8192;
    private static final int NUM_THREAD = 4;
    private static final int MAX_RESUME_CHARS = 24000;

    private final ActiveResumeService activeResumeService;
    private final ResumeContextService resumeContextService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeAnalyzerService(
            ActiveResumeService activeResumeService,
            ResumeContextService resumeContextService) {

        this.activeResumeService = activeResumeService;
        this.resumeContextService = resumeContextService;

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(15))
                        .build();
    }

    public String analyzeResume(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Session ID is required."
            );
        }

        String resumeId =
                activeResumeService.getActiveResumeId(sessionId);

        if (resumeId == null || resumeId.isBlank()) {
            throw new IllegalStateException(
                    "Please upload a resume first."
            );
        }

        String fullResume =
                resumeContextService.getResumeText(resumeId);

        if (fullResume == null || fullResume.isBlank()) {
            throw new IllegalStateException(
                    "Resume content is not available. Please upload the resume again."
            );
        }

        String resume =
                prepareResumeText(fullResume);

        List<String> lines =
                buildNumberedResumeLines(resume);

        if (lines.isEmpty()) {
            throw new IllegalStateException(
                    "Could not read meaningful resume text."
            );
        }

        String rawResponse =
                callOllama(
                        buildPrompt(lines)
                );

        try {

            JsonNode root =
                    objectMapper.readTree(
                            extractJsonObject(rawResponse)
                    );

            String role =
                    extractRole(
                            root,
                            lines
                    );

            List<String> skills =
                    extractSkills(
                            root,
                            lines
                    );

            List<String> strengths =
                    extractStrengths(
                            root,
                            lines
                    );

            List<String> improve =
                    extractRecommendations(
                            root,
                            "improve"
                    );

            List<String> ats =
                    extractRecommendations(
                            root,
                            "ats"
                    );

            List<String> next =
                    extractRecommendations(
                            root,
                            "next"
                    );

            List<String> weakMissing =
                    buildWeakMissing();

            if (role.isBlank()) {
                role =
                        detectRoleFromResume(lines);
            }

            if (role.isBlank()) {
                role =
                        "Not clearly specified";
            }

            if (skills.isEmpty()) {
                skills =
                        buildFallbackSkills(lines);
            }

            if (strengths.isEmpty()
                    || containsGenericStrength(strengths)) {

                strengths =
                        buildFallbackStrengths(
                                role,
                                skills,
                                lines
                        );
            }

            improve =
                    sanitizeImprovements(
                            improve,
                            lines
                    );

            if (improve.isEmpty()) {
                improve =
                        buildFallbackImprovements(lines);
            }

            ats =
                    sanitizeAts(
                            ats,
                            lines
                    );

            if (ats.isEmpty()) {
                ats =
                        buildFallbackAts(lines);
            }

            next =
                    sanitizeNext(
                            next,
                            lines
                    );

            if (next.isEmpty()) {
                next =
                        buildFallbackNext(lines);
            }

            String overall =
                    buildOverall(
                            role,
                            lines
                    );

            return buildFinalResult(
                    role,
                    overall,
                    strengths,
                    improve,
                    weakMissing,
                    ats,
                    next
            );

        } catch (Exception e) {

            System.out.println(
                    ">>> RESUME ANALYZER PARSING ERROR <<<"
            );

            e.printStackTrace();

            System.out.println(
                    ">>> OLLAMA RAW RESPONSE <<<"
            );

            System.out.println(rawResponse);

            throw new IllegalStateException(
                    "AI returned an invalid analyzer response: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private String prepareResumeText(
            String resume) {

        String cleaned =
                resume
                        .replace("\u0000", " ")
                        .replaceAll("[ \\t]+", " ")
                        .replaceAll("\\n{3,}", "\n\n")
                        .trim();

        if (cleaned.length() <= MAX_RESUME_CHARS) {
            return cleaned;
        }

        int half =
                MAX_RESUME_CHARS / 2;

        return cleaned.substring(
                0,
                half
        )
                + "\n\n[ MIDDLE OMITTED BECAUSE THE RESUME IS VERY LARGE ]\n\n"
                + cleaned.substring(
                cleaned.length() - half
        );
    }

    private String buildPrompt(
            List<String> lines) {

        StringBuilder resumeBlock =
                new StringBuilder();

        for (String line : lines) {
            resumeBlock
                    .append(line)
                    .append("\n");
        }

        return """
                You are a professional resume analysis assistant.

                The resume may belong to ANY profession:
                technology, HR, finance, marketing, design, education,
                healthcare, operations, sales, legal, research, or another field.

                NEVER assume the candidate is technical.
                NEVER assume the candidate is a software engineer.

                Analyze ONLY the supplied resume.

                ========================================
                CRITICAL GROUNDING
                ========================================

                You may ONLY use information explicitly present in the resume.

                NEVER invent:
                - technologies
                - tools
                - skills
                - companies
                - employers
                - clients
                - projects
                - internships
                - certifications
                - degrees
                - job titles
                - responsibilities
                - achievements
                - metrics
                - dates
                - years of experience
                - locations
                - industries

                Do not use outside knowledge as evidence.

                ========================================
                ROLE
                ========================================

                Identify the clearest current or most recent professional
                role explicitly written in the resume.

                IMPORTANT:
                - Return ONLY the actual job title.
                - Do NOT include employment dates.
                - Do NOT include company names.
                - Do NOT include locations.
                - Do NOT include surrounding text.
                - The title must appear literally in a numbered resume line.

                Example:
                If a resume line says:
                "Software Engineer Nov 2024 – Present"
                return:
                "Software Engineer"

                Do NOT return:
                "Software Engineer Nov 2024"

                Return:
                {
                  "title": "",
                  "line": 0
                }

                ========================================
                SKILLS
                ========================================

                Return up to 8 explicitly listed skills.

                Each skill must appear literally in its selected resume line.

                Prefer meaningful technical/professional skills over generic
                soft-skill phrases when both are available.

                ========================================
                STRENGTHS
                ========================================

                Return up to 2 meaningful strengths.

                Prefer:
                - demonstrated technical skills
                - domain expertise
                - hands-on project work
                - quantified achievements
                - relevant professional experience

                Avoid vague statements such as:
                "passionate"
                "hardworking"
                "good communication"

                Every strength must be supported by one numbered resume line.

                ========================================
                IMPROVE
                ========================================

                Return exactly 2 useful resume improvement recommendations.

                These are recommendations, not facts.

                IMPORTANT:
                Base the recommendations on actual weaknesses visible in THIS
                resume.

                Good examples:
                - add measurable impact to responsibility-only bullets
                - shorten overly long bullets
                - make technical contribution clearer
                - reduce repeated wording
                - improve consistency of date formatting
                - improve readability of dense bullet points
                - make achievements more prominent
                - clarify ownership or individual contribution

                Do NOT recommend:
                - adding a Summary if a Summary already exists
                - adding a Skills section if one already exists
                - adding experience if experience already exists
                - adding projects if projects already exist
                - adding certifications if certifications already exist

                Do not recommend adding information that is already present.

                ========================================
                ATS
                ========================================

                Return exactly 2 ATS/resume observations.

                Base them only on the visible structure/content.

                Focus on:
                - keyword clarity
                - section organization
                - consistent formatting
                - readability
                - measurable evidence
                - clear role terminology

                Do not say:
                "add a Skills section"
                when a Skills section already exists.

                Do not say:
                "add a Summary"
                when a Summary already exists.

                ========================================
                NEXT
                ========================================

                Return exactly 2 practical actions that improve the CURRENT
                resume.

                These should be concrete editing/improvement actions.

                Do not invent:
                - companies
                - recruiters
                - job opportunities
                - internships
                - projects
                - certifications

                Avoid generic advice such as:
                "take more courses"
                unless a clear learning gap is visible in the resume.

                ========================================
                WEAK/MISSING
                ========================================

                Return [].

                Skill gaps require a target job description.
                This application handles role-specific gap analysis separately.

                ========================================
                OUTPUT
                ========================================

                Return ONLY valid JSON.

                Do not use markdown.
                Do not add explanation outside JSON.

                Use exactly this structure:

                {
                  "role": {
                    "title": "",
                    "line": 0
                  },
                  "skills": [
                    {
                      "name": "",
                      "line": 0
                    }
                  ],
                  "strengths": [
                    {
                      "text": "",
                      "line": 0
                    }
                  ],
                  "improve": [],
                  "ats": [],
                  "next": []
                }

                ========================================
                NUMBERED RESUME
                ========================================

                %s

                ========================================
                END RESUME
                ========================================
                """.formatted(resumeBlock);
    }

    private List<String> buildNumberedResumeLines(
            String resume) {

        List<String> result =
                new ArrayList<>();

        String[] rawLines =
                resume.split("\\R");

        int number = 1;

        for (String rawLine : rawLines) {

            String cleaned =
                    rawLine
                            .replaceAll(
                                    "\\s+",
                                    " "
                            )
                            .trim();

            if (cleaned.isBlank()) {
                continue;
            }

            result.add(
                    number + ": " + cleaned
            );

            number++;
        }

        return result;
    }

    private String extractRole(
            JsonNode root,
            List<String> lines) {

        JsonNode roleNode =
                root.get("role");

        if (roleNode == null
                || !roleNode.isObject()) {

            return "";
        }

        String title =
                cleanText(
                        roleNode
                                .path("title")
                                .asText("")
                );

        int lineNumber =
                roleNode
                        .path("line")
                        .asInt(-1);

        if (title.isBlank()
                || lineNumber < 1
                || lineNumber > lines.size()) {

            return "";
        }

        String evidence =
                getLineText(
                        lines,
                        lineNumber
                );

        if (evidence.isBlank()) {
            return "";
        }

        if (!containsIgnoreCase(
                evidence,
                title
        )) {

            return "";
        }

        return cleanRoleTitle(
                title
        );
    }

    private String cleanRoleTitle(
            String title) {

        if (title == null
                || title.isBlank()) {

            return "";
        }

        String cleaned =
                cleanText(title);

        cleaned =
                cleaned.replaceAll(
                        "(?i)\\b(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)[a-z]*\\s+\\d{4}\\b.*$",
                        ""
                );

        cleaned =
                cleaned.replaceAll(
                        "\\b\\d{4}\\b.*$",
                        ""
                );

        cleaned =
                cleaned.replaceAll(
                        "\\s{2,}",
                        " "
                );

        return cleaned.trim();
    }

    private String detectRoleFromResume(
            List<String> lines) {

        String[] roleHints = {
                "software engineer",
                "software developer",
                "backend developer",
                "backend engineer",
                "full stack developer",
                "full-stack developer",
                "frontend developer",
                "frontend engineer",
                "web developer",
                "data engineer",
                "data engineering",
                "data analyst",
                "data scientist",
                "business analyst",
                "project manager",
                "product manager",
                "human resources",
                "hr",
                "marketing",
                "designer",
                "accountant",
                "financial analyst",
                "operations",
                "sales",
                "intern",
                "analyst"
        };

        for (String line : lines) {

            String body =
                    getLineBody(line);

            String lower =
                    body.toLowerCase(Locale.ROOT);

            for (String hint : roleHints) {

                if (lower.contains(hint)) {

                    return extractRolePhrase(
                            body,
                            hint
                    );
                }
            }
        }

        return "";
    }

    private String extractRolePhrase(
            String line,
            String hint) {

        String lower =
                line.toLowerCase(Locale.ROOT);

        int index =
                lower.indexOf(hint);

        if (index < 0) {
            return "";
        }

        /*
         * Return ONLY the matched role phrase.
         * This prevents dates such as "Nov 2024" or company names
         * from being appended to the role.
         */
        return line.substring(
                        index,
                        index + hint.length()
                )
                .trim();
    }

    private List<String> extractSkills(
            JsonNode root,
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        JsonNode skillsNode =
                root.get("skills");

        if (skillsNode == null
                || !skillsNode.isArray()) {

            return result;
        }

        for (JsonNode skillNode :
                skillsNode) {

            String name =
                    cleanText(
                            skillNode
                                    .path("name")
                                    .asText("")
                    );

            int lineNumber =
                    skillNode
                            .path("line")
                            .asInt(-1);

            if (name.isBlank()
                    || lineNumber < 1
                    || lineNumber > lines.size()) {

                continue;
            }

            String evidence =
                    getLineText(
                            lines,
                            lineNumber
                    );

            if (containsIgnoreCase(
                    evidence,
                    name
            )) {

                addUnique(
                        result,
                        name
                );
            }

            if (result.size() >= 8) {
                break;
            }
        }

        return result;
    }

    private List<String> buildFallbackSkills(
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        boolean inSkills =
                false;

        for (String numberedLine : lines) {

            String body =
                    getLineBody(numberedLine);

            String lower =
                    normalize(body);

            if (isSkillsHeading(body)) {
                inSkills = true;
                continue;
            }

            if (inSkills
                    && isMajorSection(body)
                    && !isSkillsHeading(body)) {

                inSkills = false;
            }

            if (inSkills) {

                String[] parts =
                        body.split("[|,;]");

                for (String part : parts) {

                    String skill =
                            cleanText(part);

                    if (skill.length() >= 2
                            && skill.length() <= 60
                            && !isCategoryLabel(skill)) {

                        addUnique(
                                result,
                                skill
                        );
                    }

                    if (result.size() >= 8) {
                        break;
                    }
                }
            }

            if (result.size() >= 8) {
                break;
            }

            if (!inSkills
                    && (lower.startsWith("technologies:")
                    || lower.startsWith("technology:")
                    || lower.startsWith("skills:"))) {

                int colon =
                        body.indexOf(':');

                if (colon >= 0
                        && colon + 1 < body.length()) {

                    String value =
                            body.substring(
                                    colon + 1
                            );

                    for (String part :
                            value.split("[|,;]")) {

                        String skill =
                                cleanText(part);

                        if (!skill.isBlank()) {
                            addUnique(
                                    result,
                                    skill
                            );
                        }

                        if (result.size() >= 8) {
                            break;
                        }
                    }
                }
            }
        }

        return limit(
                result,
                8
        );
    }

    private List<String> extractStrengths(
            JsonNode root,
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        JsonNode strengthsNode =
                root.get("strengths");

        if (strengthsNode == null
                || !strengthsNode.isArray()) {

            return result;
        }

        for (JsonNode strengthNode :
                strengthsNode) {

            String text =
                    cleanText(
                            strengthNode
                                    .path("text")
                                    .asText("")
                    );

            int lineNumber =
                    strengthNode
                            .path("line")
                            .asInt(-1);

            if (text.isBlank()
                    || lineNumber < 1
                    || lineNumber > lines.size()) {

                continue;
            }

            String evidence =
                    getLineText(
                            lines,
                            lineNumber
                    );

            if (evidence.isBlank()) {
                continue;
            }

            String strength =
                    text
                            + " (Evidence: "
                            + evidence
                            + ")";

            addUnique(
                    result,
                    strength
            );

            if (result.size() >= 2) {
                break;
            }
        }

        return result;
    }

    private boolean containsGenericStrength(
            List<String> strengths) {

        for (String strength : strengths) {

            String lower =
                    strength.toLowerCase(
                            Locale.ROOT
                    );

            if (lower.contains("passionate")
                    || lower.contains("hardworking")
                    || lower.contains("good communication")
                    || lower.contains("dedicated")
                    || lower.contains("quick learner")) {

                return true;
            }
        }

        return false;
    }

    private List<String> buildFallbackStrengths(
            String role,
            List<String> skills,
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        List<String> technicalSkills =
                extractStrongSkills(lines);

        if (!technicalSkills.isEmpty()) {

            result.add(
                    "The resume demonstrates relevant skills including "
                            + String.join(
                            ", ",
                            technicalSkills
                    )
                            + "."
            );
        }

        String evidence =
                findStrongEvidenceLine(lines);

        if (!evidence.isBlank()) {

            result.add(
                    "The resume includes hands-on evidence: "
                            + evidence
            );
        }

        if (result.size() < 2
                && role != null
                && !role.isBlank()
                && !role.equals(
                "Not clearly specified"
        )) {

            result.add(
                    "The resume clearly identifies the professional role as "
                            + role
                            + "."
            );
        }

        if (result.size() < 2
                && !skills.isEmpty()) {

            result.add(
                    "The resume explicitly lists "
                            + String.join(
                            ", ",
                            skills.subList(
                                    0,
                                    Math.min(
                                            5,
                                            skills.size()
                                    )
                            )
                    )
                            + "."
            );
        }

        if (result.isEmpty()) {

            result.add(
                    "The resume contains explicit professional information and supporting evidence."
            );
        }

        return limit(
                result,
                2
        );
    }

    private List<String> extractStrongSkills(
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        String[] common = {
                "java",
                "python",
                "javascript",
                "typescript",
                "spring boot",
                "spring security",
                "rest apis",
                "sql",
                "mysql",
                "postgresql",
                "mongodb",
                "docker",
                "kubernetes",
                "react",
                "node.js",
                "express.js",
                "pandas",
                "numpy",
                "excel",
                "power bi",
                "tableau",
                "data analysis",
                "data visualization",
                "git",
                "github",
                "kafka",
                "ibm mq",
                "rabbitmq",
                "aws",
                "microservices",
                "spring cloud",
                "hibernate",
                "jpa",
                "ci/cd",
                "jenkins",
                "kibana",
                "grafana",
                "prometheus",
                "mongodb"
        };

        String resume =
                normalize(
                        String.join(
                                " ",
                                lines
                        )
                );

        for (String skill :
                common) {

            if (containsWordOrPhrase(
                    resume,
                    normalize(skill)
            )) {

                addUnique(
                        result,
                        skill
                );
            }

            if (result.size() >= 5) {
                break;
            }
        }

        return result;
    }

    private String findStrongEvidenceLine(
            List<String> lines) {

        for (String numberedLine :
                lines) {

            String body =
                    getLineBody(numberedLine);

            String lower =
                    body.toLowerCase(
                            Locale.ROOT
                    );

            if (isEvidenceActionLine(lower)
                    && !isContactOrHeaderLine(body)) {

                return body;
            }
        }

        return "";
    }

    private List<String> extractRecommendations(
            JsonNode root,
            String field) {

        List<String> result =
                new ArrayList<>();

        JsonNode node =
                root.get(field);

        if (node == null
                || !node.isArray()) {

            return result;
        }

        for (JsonNode item :
                node) {

            String text =
                    cleanText(
                            item.asText("")
                    );

            if (text.isBlank()) {
                continue;
            }

            if (containsInventedOpportunity(text)) {
                continue;
            }

            addUnique(
                    result,
                    text
            );

            if (result.size() >= 2) {
                break;
            }
        }

        return result;
    }

    private List<String> sanitizeImprovements(
            List<String> values,
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        boolean hasSummary =
                hasSection(
                        lines,
                        "summary"
                );

        boolean hasSkills =
                hasSkillsSection(lines);

        boolean hasExperience =
                hasSection(
                        lines,
                        "experience"
                );

        boolean hasProjects =
                hasSection(
                        lines,
                        "projects"
                );

        for (String value : values) {

            if (value == null
                    || value.isBlank()) {
                continue;
            }

            String lower =
                    normalize(value);

            if (hasSummary
                    && (
                    lower.contains("add a professional summary")
                            || lower.contains("add summary")
                            || lower.contains("add a summary")
                            || lower.contains("create a summary")
            )) {

                continue;
            }

            if (hasSkills
                    && (
                    lower.contains("add a skills section")
                            || lower.contains("add skills section")
                            || lower.contains("create a skills section")
                            || lower.contains("add technical skills")
            )) {

                continue;
            }

            if (hasExperience
                    && (
                    lower.contains("add work experience")
                            || lower.contains("add professional experience")
            )) {

                continue;
            }

            if (hasProjects
                    && (
                    lower.contains("add projects section")
                            || lower.contains("add projects")
            )) {

                continue;
            }

            if (containsInventedOpportunity(value)) {
                continue;
            }

            addUnique(
                    result,
                    value
            );

            if (result.size() >= 2) {
                break;
            }
        }

        return result;
    }

    private List<String> sanitizeAts(
            List<String> values,
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        boolean hasSkills =
                hasSkillsSection(lines);

        boolean hasSummary =
                hasSection(
                        lines,
                        "summary"
                );

        for (String value : values) {

            if (value == null
                    || value.isBlank()) {
                continue;
            }

            String lower =
                    normalize(value);

            if (hasSkills
                    && (
                    lower.contains("add a skills section")
                            || lower.contains("create a skills section")
                            || lower.contains("add skills section")
                            || lower.contains("add technical skills")
            )) {

                continue;
            }

            if (hasSummary
                    && (
                    lower.contains("add a summary")
                            || lower.contains("add a professional summary")
                            || lower.contains("create a summary")
            )) {

                continue;
            }

            if (containsInventedOpportunity(value)) {
                continue;
            }

            addUnique(
                    result,
                    value
            );

            if (result.size() >= 2) {
                break;
            }
        }

        return result;
    }

    private List<String> sanitizeNext(
            List<String> values,
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        for (String value : values) {

            if (value == null
                    || value.isBlank()) {
                continue;
            }

            if (containsInventedOpportunity(value)) {
                continue;
            }

            if (isGenericCourseAdvice(value)) {
                continue;
            }

            addUnique(
                    result,
                    value
            );

            if (result.size() >= 2) {
                break;
            }
        }

        return result;
    }

    private boolean isGenericCourseAdvice(
            String value) {

        String lower =
                normalize(value);

        return lower.contains("take an online course")
                || lower.contains("take additional courses")
                || lower.contains("take more courses")
                || lower.contains("get more certifications")
                || lower.contains("earn more certifications");
    }

    private List<String> buildFallbackImprovements(
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        result.add(
                "Make the strongest experience bullets more specific by clearly describing the contribution, technical work and outcome."
        );

        if (!containsAnyLine(
                lines,
                "%",
                "reduced",
                "increased",
                "improved",
                "achieved",
                "saved",
                "processed",
                "managed",
                "generated",
                "million",
                "50+"
        )) {

            result.add(
                    "Add measurable results or concrete scope wherever the resume currently describes work without clear impact."
            );

        } else {

            result.add(
                    "Keep measurable achievements directly connected to the work or experience they describe."
            );
        }

        return result;
    }

    private List<String> buildFallbackAts(
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        if (hasSkillsSection(lines)) {

            result.add(
                    "Keep technical skill names consistent and grouped logically so important keywords are easy to scan."
            );

        } else {

            result.add(
                    "Add a clearly labeled skills section if the target role requires easy identification of key capabilities."
            );
        }

        result.add(
                "Keep section headings, dates and bullet formatting consistent throughout the resume."
        );

        return result;
    }

    private List<String> buildFallbackNext(
            List<String> lines) {

        List<String> result =
                new ArrayList<>();

        result.add(
                "Rewrite the strongest resume bullets with clearer evidence of contribution and measurable outcome."
        );

        if (hasSection(
                lines,
                "projects"
        )
                || hasSection(
                lines,
                "project"
        )) {

            result.add(
                    "Prioritize the most relevant project and make its technologies, contribution and outcome easy to scan."
            );

        } else {

            result.add(
                    "Review the resume against the target role and improve only the sections that are relevant to that role."
            );
        }

        return result;
    }

    private List<String> buildWeakMissing() {

        return List.of(
                "No specific skill gap is determined from the resume alone. Provide a target job description for role-specific gap analysis."
        );
    }

    private String buildOverall(
            String role,
            List<String> lines) {

        if (role != null
                && !role.isBlank()
                && !role.equals(
                "Not clearly specified"
        )) {

            return "The resume presents a "
                    + role
                    + " profile based on the information explicitly included in the document.";
        }

        return "The resume presents a professional profile based on the information explicitly included in the document.";
    }

    private boolean hasSection(
            List<String> lines,
            String section) {

        String target =
                normalize(section);

        for (String line :
                lines) {

            String text =
                    normalize(
                            getLineBody(line)
                    );

            if (isSectionMatch(
                    text,
                    target
            )) {

                return true;
            }
        }

        return false;
    }

    private boolean hasSkillsSection(
            List<String> lines) {

        for (String line :
                lines) {

            if (isSkillsHeading(
                    getLineBody(line)
            )) {

                return true;
            }
        }

        return false;
    }

    private boolean isSkillsHeading(
            String line) {

        String lower =
                normalize(line);

        return lower.equals("skills")
                || lower.equals("key skills")
                || lower.equals("technical skills")
                || lower.equals("core skills")
                || lower.equals("technical competencies")
                || lower.equals("skills and competencies")
                || lower.equals("technical skills and tools");
    }

    private boolean isMajorSection(
            String line) {

        String lower =
                normalize(line);

        return lower.equals("summary")
                || lower.equals("professional summary")
                || lower.equals("profile")
                || lower.equals("objective")
                || lower.equals("skills")
                || lower.equals("key skills")
                || lower.equals("technical skills")
                || lower.equals("technical competencies")
                || lower.equals("experience")
                || lower.equals("professional experience")
                || lower.equals("work experience")
                || lower.equals("projects")
                || lower.equals("project")
                || lower.equals("education")
                || lower.equals("certifications")
                || lower.equals("achievements")
                || lower.equals("additional information")
                || lower.equals("languages");
    }

    private boolean isSectionMatch(
            String text,
            String target) {

        if (text.equals(target)) {
            return true;
        }

        if (target.equals("summary")) {

            return text.equals(
                    "professional summary"
            )
                    || text.equals(
                    "profile"
            )
                    || text.equals(
                    "career summary"
            );
        }

        if (target.equals("projects")) {

            return text.equals(
                    "project"
            )
                    || text.equals(
                    "academic projects"
            )
                    || text.equals(
                    "personal projects"
            );
        }

        if (target.equals("experience")) {

            return text.equals(
                    "professional experience"
            )
                    || text.equals(
                    "work experience"
            )
                    || text.equals(
                    "employment"
            );
        }

        return false;
    }

    private boolean containsAnyLine(
            List<String> lines,
            String... values) {

        for (String line :
                lines) {

            String text =
                    normalize(
                            getLineBody(line)
                    );

            for (String value :
                    values) {

                if (text.contains(
                        normalize(value)
                )) {

                    return true;
                }
            }
        }

        return false;
    }

    private String getLineText(
            List<String> lines,
            int lineNumber) {

        if (lineNumber < 1
                || lineNumber > lines.size()) {

            return "";
        }

        return getLineBody(
                lines.get(
                        lineNumber - 1
                )
        );
    }

    private String getLineBody(
            String numberedLine) {

        if (numberedLine == null) {
            return "";
        }

        int colon =
                numberedLine.indexOf(": ");

        if (colon < 0) {
            return numberedLine.trim();
        }

        return numberedLine
                .substring(
                        colon + 2
                )
                .trim();
    }

    private boolean containsIgnoreCase(
            String text,
            String value) {

        return text != null
                && value != null
                && text.toLowerCase(
                Locale.ROOT
        ).contains(
                value.toLowerCase(
                        Locale.ROOT
                )
        );
    }

    private boolean containsWordOrPhrase(
            String text,
            String phrase) {

        String paddedText =
                " "
                        + text
                        + " ";

        String paddedPhrase =
                " "
                        + phrase
                        + " ";

        return paddedText.contains(
                paddedPhrase
        );
    }

    private boolean isEvidenceActionLine(
            String lower) {

        return lower.startsWith("developed ")
                || lower.startsWith("built ")
                || lower.startsWith("implemented ")
                || lower.startsWith("designed ")
                || lower.startsWith("created ")
                || lower.startsWith("managed ")
                || lower.startsWith("worked ")
                || lower.startsWith("integrated ")
                || lower.startsWith("optimized ")
                || lower.startsWith("engineered ")
                || lower.startsWith("architected ")
                || lower.startsWith("analyzed ")
                || lower.startsWith("tracked ")
                || lower.startsWith("monitored ")
                || lower.startsWith("coordinated ")
                || lower.startsWith("improved ")
                || lower.startsWith("configured ")
                || lower.startsWith("deployed ")
                || lower.startsWith("automated ")
                || lower.startsWith("tested ");
    }

    private boolean isContactOrHeaderLine(
            String line) {

        String lower =
                line.toLowerCase(
                        Locale.ROOT
                );

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

        if (line.matches(
                ".*\\d{10,}.*"
        )) {

            return true;
        }

        return false;
    }

    private boolean containsInventedOpportunity(
            String text) {

        String lower =
                normalize(text);

        return lower.contains(
                "reach out to"
        )
                || lower.contains(
                "contact a recruiter"
        )
                || lower.contains(
                "apply to a company"
        )
                || lower.contains(
                "job opportunity"
        )
                || lower.contains(
                "internship opportunity"
        )
                || lower.contains(
                "network with recruiters"
        )
                || lower.contains(
                "contact recruiters"
        )
                || lower.contains(
                "reach recruiters"
        );
    }

    private boolean isCategoryLabel(
            String value) {

        String lower =
                normalize(value);

        return lower.equals("programming")
                || lower.equals("languages")
                || lower.equals("backend")
                || lower.equals("backend and database")
                || lower.equals("web technologies")
                || lower.equals("data analysis")
                || lower.equals("visualization")
                || lower.equals("tools")
                || lower.equals("technical skills")
                || lower.equals("core concepts");
    }

    private String cleanText(
            String text) {

        if (text == null) {
            return "";
        }

        return text
                .replaceAll(
                        "^[\\-*•\\d\\.\\)]+\\s*",
                        ""
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private String normalize(
            String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase(
                        Locale.ROOT
                )
                .replace(
                        "&",
                        " and "
                )
                .replace(
                        "powerbi",
                        "power bi"
                )
                .replace(
                        "springboot",
                        "spring boot"
                )
                .replace(
                        "reactjs",
                        "react js"
                )
                .replaceAll(
                        "[^a-z0-9+#./-]+",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private void addUnique(
            List<String> list,
            String value) {

        String normalized =
                normalize(value);

        for (String existing :
                list) {

            if (normalize(existing)
                    .equals(normalized)) {

                return;
            }
        }

        list.add(value);
    }

    private List<String> limit(
            List<String> values,
            int max) {

        if (values == null) {
            return new ArrayList<>();
        }

        if (values.size() <= max) {
            return values;
        }

        return new ArrayList<>(
                values.subList(
                        0,
                        max
                )
        );
    }

    private String buildFinalResult(
            String role,
            String overall,
            List<String> strengths,
            List<String> improve,
            List<String> weakMissing,
            List<String> ats,
            List<String> next) {

        StringBuilder result =
                new StringBuilder();

        result.append(
                "ROLE:\n"
        ).append(
                role
        );

        result.append(
                "\n\nOVERALL:\n"
        ).append(
                overall
        );

        result.append(
                "\n\nSTRENGTHS:\n"
        );

        appendItems(
                result,
                strengths
        );

        result.append(
                "\nIMPROVE:\n"
        );

        appendItems(
                result,
                improve
        );

        result.append(
                "\nWEAK/MISSING:\n"
        );

        appendItems(
                result,
                weakMissing
        );

        result.append(
                "\nATS:\n"
        );

        appendItems(
                result,
                ats
        );

        result.append(
                "\nNEXT:\n"
        );

        appendItems(
                result,
                next
        );

        return result
                .toString()
                .trim();
    }

    private void appendItems(
            StringBuilder result,
            List<String> items) {

        if (items == null
                || items.isEmpty()) {

            result.append(
                    "- None identified.\n"
            );

            return;
        }

        for (String item :
                items) {

            result.append(
                            "- "
                    )
                    .append(item)
                    .append("\n");
        }
    }

    private String extractJsonObject(
            String text) {

        if (text == null
                || text.isBlank()) {

            throw new IllegalStateException(
                    "Ollama returned an empty response."
            );
        }

        int start =
                text.indexOf("{");

        int end =
                text.lastIndexOf("}");

        if (start < 0
                || end <= start) {

            throw new IllegalStateException(
                    "No JSON object found in Ollama response."
            );
        }

        return text.substring(
                start,
                end + 1
        );
    }

    private String callOllama(
            String prompt) {

        try {

            Map<String, Object> options =
                    Map.of(
                            "temperature",
                            0.0,
                            "num_predict",
                            NUM_PREDICT,
                            "num_ctx",
                            NUM_CTX,
                            "num_thread",
                            NUM_THREAD
                    );

            Map<String, Object> requestBody =
                    Map.of(
                            "model",
                            MODEL,
                            "prompt",
                            prompt,
                            "stream",
                            false,
                            "format",
                            "json",
                            "options",
                            options
                    );

            String requestJson =
                    objectMapper.writeValueAsString(
                            requestBody
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            OLLAMA_URL
                                    )
                            )
                            .timeout(
                                    Duration.ofSeconds(
                                            300
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    requestJson
                                            )
                            )
                            .build();

            System.out.println(
                    ">>> Calling Ollama: "
                            + OLLAMA_URL
                            + " | Model: "
                            + MODEL
            );

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    ">>> Ollama HTTP status: "
                            + response.statusCode()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "Ollama returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            JsonNode responseNode =
                    root.get("response");

            if (responseNode == null
                    || responseNode.isNull()) {

                throw new IllegalStateException(
                        "Ollama returned no response text. Raw response: "
                                + response.body()
                );
            }

            String result =
                    responseNode
                            .asText("")
                            .trim();

            if (result.isBlank()) {

                throw new IllegalStateException(
                        "Ollama returned an empty response."
                );
            }

            return result;

        } catch (IOException e) {

            System.out.println(
                    ">>> OLLAMA IO ERROR <<<"
            );

            e.printStackTrace();

            throw new IllegalStateException(
                    "Ollama request failed: "
                            + e.getMessage(),
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println(
                    ">>> OLLAMA REQUEST INTERRUPTED <<<"
            );

            e.printStackTrace();

            throw new IllegalStateException(
                    "Ollama request was interrupted: "
                            + e.getMessage(),
                    e
            );
        }
    }
}
package com.hr.AICareerCopilot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Deterministic skill-gap analyzer.
 *
 * Classification:
 * DEMONSTRATED -> skill is clearly present in the resume
 * KEYWORD_ONLY -> skill is present but evidence is limited
 * ABSENT -> skill is not present in the resume
 */
@Service
public class SkillGapAnalyzerService {

    private final ActiveResumeService activeResumeService;
    private final ResumeContextService resumeContextService;

    public SkillGapAnalyzerService(
            ActiveResumeService activeResumeService,
            ResumeContextService resumeContextService) {

        this.activeResumeService =
                activeResumeService;

        this.resumeContextService =
                resumeContextService;
    }

    public String analyzeSkillGap(
            String jobDescription,
            String sessionId) {

        if (!activeResumeService.hasActiveResume(sessionId)) {
            return "Please upload a resume first.";
        }

        if (jobDescription == null ||
                jobDescription.isBlank()) {

            return "Please provide a job description.";
        }

        String resumeId =
                activeResumeService.getActiveResumeId(
                        sessionId
                );

        String resume =
                resumeContextService.getResumeText(
                        resumeId
                );

        if (resume == null ||
                resume.isBlank()) {

            return "Resume content is not available. Please upload the resume again.";
        }

        List<String> requirements =
                extractRequirements(
                        jobDescription
                );

        if (requirements.isEmpty()) {

            return "Could not identify technical requirements from the job description.";
        }

        List<String> alreadyHave =
                new ArrayList<>();

        List<String> needsImprovement =
                new ArrayList<>();

        List<String> missing =
                new ArrayList<>();

        for (String requirement :
                requirements) {

            Presence presence =
                    classifyPresence(
                            requirement,
                            resume
                    );

            switch (presence) {

                case DEMONSTRATED ->
                        alreadyHave.add(requirement);

                case KEYWORD_ONLY ->
                        needsImprovement.add(requirement);

                case ABSENT ->
                        missing.add(requirement);
            }
        }

        List<String> priority =
                buildPriority(
                        needsImprovement,
                        missing
                );

        List<String> learningPlan =
                buildLearningPlan(
                        priority
                );

        StringBuilder result =
                new StringBuilder();

        result.append(
                "✅ ALREADY HAVE\n"
        );

        append(
                result,
                alreadyHave
        );

        result.append(
                "\n⚠️ NEEDS IMPROVEMENT\n"
        );

        append(
                result,
                needsImprovement
        );

        result.append(
                "\n❌ MISSING\n"
        );

        append(
                result,
                missing
        );

        result.append(
                "\n🎯 LEARNING PRIORITY\n"
        );

        appendNumbered(
                result,
                priority
        );

        result.append(
                "\n📚 PRACTICAL LEARNING PLAN\n"
        );

        append(
                result,
                learningPlan
        );

        return result
                .toString()
                .trim();
    }

    public String analyze(
            String jobDescription,
            String sessionId) {

        return analyzeSkillGap(
                jobDescription,
                sessionId
        );
    }

    private List<String> extractRequirements(
            String jd) {

        String lower =
                jd.toLowerCase(
                        Locale.ROOT
                );

        Set<String> found =
                new LinkedHashSet<>();

        String[][] aliases = {

                {"java", "Java"},

                {"spring boot",
                        "Spring Boot"},

                {"spring data jpa",
                        "Spring Data JPA"},

                {"spring security",
                        "Spring Security"},

                {"hibernate",
                        "Hibernate"},

                {"jpa",
                        "JPA"},

                {"rest apis",
                        "REST APIs"},

                {"rest api",
                        "REST APIs"},

                {"restful apis",
                        "REST APIs"},

                {"backend development",
                        "Backend development"},

                {"backend",
                        "Backend development"},

                {"object-oriented programming",
                        "Object-Oriented Programming (OOP)"},

                {"oop",
                        "Object-Oriented Programming (OOP)"},

                {"data structures and algorithms",
                        "Data Structures and Algorithms"},

                {"data structures",
                        "Data Structures and Algorithms"},

                {"algorithms",
                        "Data Structures and Algorithms"},

                {"sql",
                        "SQL"},

                {"postgresql",
                        "PostgreSQL"},

                {"mysql",
                        "MySQL"},

                {"mongodb",
                        "MongoDB"},

                {"docker",
                        "Docker"},

                {"kubernetes",
                        "Kubernetes"},

                {"git",
                        "Git"},

                {"github",
                        "GitHub"},

                {"microservices",
                        "Microservices"},

                {"aws",
                        "AWS"},

                {"azure",
                        "Azure"},

                {"gcp",
                        "GCP"},

                {"react.js",
                        "React.js"},

                {"react",
                        "React.js"},

                {"javascript",
                        "JavaScript"},

                {"typescript",
                        "TypeScript"},

                {"python",
                        "Python"},

                {"junit",
                        "JUnit"},

                {"testing",
                        "Testing"},

                {"jwt",
                        "JWT"},

                {"oauth",
                        "OAuth"},

                {"flyway",
                        "Flyway"},

                {"kafka",
                        "Kafka"},

                {"redis",
                        "Redis"}
        };

        for (String[] pair :
                aliases) {

            if (lower.contains(
                    pair[0]
            )) {

                found.add(
                        pair[1]
                );
            }
        }

        List<String> result =
                new ArrayList<>(
                        found
                );

        return result.subList(
                0,
                Math.min(
                        result.size(),
                        12
                )
        );
    }

    private Presence classifyPresence(
            String requirement,
            String resume) {

        String whole =
                normalize(resume);

        String key =
                normalize(requirement);

        boolean present =
                isPresent(
                        key,
                        whole
                );

        if (!present) {
            return Presence.ABSENT;
        }

        /*
         * For the skill-gap feature we intentionally treat
         * clearly listed technical skills as demonstrated.
         *
         * This keeps Skill Gap consistent with Job Matcher
         * and avoids incorrectly marking explicit resume skills
         * such as PostgreSQL, Git, Docker and Spring Security
         * as missing.
         */
        if (hasClearResumeEvidence(
                key,
                resume
        )) {

            return Presence.DEMONSTRATED;
        }

        return Presence.KEYWORD_ONLY;
    }

    private boolean isPresent(
            String key,
            String whole) {

        return switch (key) {

            case "spring boot" ->
                    whole.contains("spring boot")
                            || whole.contains("springboot");

            case "rest apis" ->
                    whole.contains("rest api")
                            || whole.contains("rest apis")
                            || whole.contains("restful");

            case "object oriented programming oop" ->
                    whole.contains("oop")
                            || whole.contains("object oriented");

            case "data structures and algorithms" ->
                    whole.contains("dsa")
                            || whole.contains("data structures")
                            || whole.contains("algorithms");

            case "backend development" ->
                    whole.contains("backend")
                            || whole.contains("back end");

            default ->
                    whole.contains(key);
        };
    }

    private boolean hasClearResumeEvidence(
            String key,
            String resume) {

        String whole =
                normalize(resume);

        /*
         * Strong resume evidence:
         * skills section, project section, certifications,
         * experience bullets, summary, etc.
         *
         * Explicitly listed technical skills count as
         * demonstrated for this portfolio project.
         */

        switch (key) {

            case "java":
                return whole.contains("java");

            case "spring boot":
                return whole.contains("spring boot")
                        || whole.contains("springboot");

            case "spring security":
                return whole.contains("spring security");

            case "rest apis":
                return whole.contains("rest api")
                        || whole.contains("rest apis")
                        || whole.contains("restful");

            case "sql":
                return whole.contains(" sql ")
                        || whole.startsWith("sql ")
                        || whole.endsWith(" sql")
                        || whole.contains(" sql ");

            case "postgresql":
                return whole.contains("postgresql");

            case "git":
                return hasWholeWord(
                        whole,
                        "git"
                );

            case "docker":
                return whole.contains("docker");

            case "backend development":
                return whole.contains("backend")
                        || whole.contains("back end");

            case "spring data jpa":
                return whole.contains("spring data jpa");

            case "hibernate":
                return whole.contains("hibernate");

            case "jpa":
                return hasWholeWord(
                        whole,
                        "jpa"
                );

            case "jwt":
                return whole.contains("jwt");

            case "oauth":
                return whole.contains("oauth");

            case "flyway":
                return whole.contains("flyway");

            case "microservices":
                return whole.contains("microservices");

            case "aws":
                return hasWholeWord(
                        whole,
                        "aws"
                );

            case "kafka":
                return whole.contains("kafka");

            case "kubernetes":
                return whole.contains("kubernetes");

            case "mysql":
                return whole.contains("mysql");

            case "mongodb":
                return whole.contains("mongodb");

            case "redis":
                return whole.contains("redis");

            case "python":
                return whole.contains("python");

            case "javascript":
                return whole.contains("javascript");

            case "typescript":
                return whole.contains("typescript");

            case "react js":
                return whole.contains("react");

            case "testing":
                return whole.contains("testing");

            case "junit":
                return whole.contains("junit");

            case "object oriented programming oop":
                return whole.contains("oop")
                        || whole.contains("object oriented");

            case "data structures and algorithms":
                return whole.contains("dsa")
                        || whole.contains("data structures")
                        || whole.contains("algorithms");

            case "github":
                return whole.contains("github");

            case "azure":
                return hasWholeWord(
                        whole,
                        "azure"
                );

            case "gcp":
                return hasWholeWord(
                        whole,
                        "gcp"
                );

            case "react":
                return whole.contains("react");

            default:
                return whole.contains(key);
        }
    }

    private boolean hasWholeWord(
            String text,
            String word) {

        String normalizedWord =
                normalize(word);

        return (
                text.equals(
                        normalizedWord
                )
                        || text.startsWith(
                        normalizedWord + " "
                )
                        || text.endsWith(
                        " " + normalizedWord
                )
                        || text.contains(
                        " " + normalizedWord + " "
                )
        );
    }

    private List<String> buildPriority(
            List<String> needsImprovement,
            List<String> missing) {

        List<String> result =
                new ArrayList<>();

        /*
         * Missing skills get highest priority,
         * then skills needing improvement.
         */
        for (String item :
                missing) {

            addUnique(
                    result,
                    item
            );
        }

        for (String item :
                needsImprovement) {

            addUnique(
                    result,
                    item
            );
        }

        return result.subList(
                0,
                Math.min(
                        result.size(),
                        5
                )
        );
    }

    private List<String> buildLearningPlan(
            List<String> priority) {

        List<String> plan =
                new ArrayList<>();

        for (String item :
                priority) {

            plan.add(
                    "Strengthen "
                            + item
                            + " with focused practice and one small hands-on project or implementation."
            );

            if (plan.size() == 5) {
                break;
            }
        }

        if (plan.isEmpty()) {

            plan.add(
                    "No major skill gaps were identified from the supplied job description."
            );

            plan.add(
                    "Continue building measurable project evidence for the target role."
            );
        }

        return plan;
    }

    private void append(
            StringBuilder out,
            List<String> items) {

        if (items.isEmpty()) {

            out.append(
                    "- None identified.\n"
            );

            return;
        }

        for (String item :
                items) {

            out.append(
                            "- "
                    )
                    .append(item)
                    .append("\n");
        }
    }

    private void appendNumbered(
            StringBuilder out,
            List<String> items) {

        if (items.isEmpty()) {

            out.append(
                    "- No priority gaps identified.\n"
            );

            return;
        }

        int i = 1;

        for (String item :
                items) {

            out.append(
                            i++
                    )
                    .append(". ")
                    .append(item)
                    .append("\n");
        }
    }

    private void addUnique(
            List<String> list,
            String value) {

        String n =
                normalize(value);

        for (String existing :
                list) {

            if (normalize(existing)
                    .equals(n)) {

                return;
            }
        }

        list.add(value);
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(
                        Locale.ROOT
                )
                .replace(
                        "restful",
                        "rest"
                )
                .replace(
                        "c++",
                        "cpp"
                )
                .replaceAll(
                        "[^a-z0-9+#]+",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private enum Presence {
        DEMONSTRATED,
        KEYWORD_ONLY,
        ABSENT
    }
}
package com.hr.AICareerCopilot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CareerRoadmapService {

    private final ActiveResumeService activeResumeService;
    private final ResumeContextService resumeContextService;

    public CareerRoadmapService(
            ActiveResumeService activeResumeService,
            ResumeContextService resumeContextService) {

        this.activeResumeService = activeResumeService;
        this.resumeContextService = resumeContextService;
    }

    public String generateRoadmap(
            String careerGoal,
            String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            return "Session ID is required.";
        }

        if (!activeResumeService.hasActiveResume(sessionId)) {
            return "Please upload a resume first.";
        }

        if (careerGoal == null || careerGoal.isBlank()) {
            return "Please enter your career goal.";
        }

        String resumeId =
                activeResumeService.getActiveResumeId(sessionId);

        if (resumeId == null || resumeId.isBlank()) {
            return "Please upload a resume first.";
        }

        String resume =
                resumeContextService.getResumeText(resumeId);

        if (resume == null || resume.isBlank()) {
            return "Resume content is not available. Please upload the resume again.";
        }

        String goal = careerGoal.trim();

        RoleProfile profile = selectProfile(goal);

        List<String> gaps =
                findGaps(
                        profile.focusSkills(),
                        resume
                );

        StringBuilder result = new StringBuilder();

        result.append(
                "90-DAY CAREER ROADMAP — "
        ).append(profile.name());

        result.append(
                "\n\nTARGET GOAL:\n"
        ).append(goal);

        result.append(
                "\n\nCURRENT PROFILE:\n"
        ).append(
                buildCurrentProfile(resume, profile)
        );

        result.append(
                "\n\nIDENTIFIED GAPS:\n"
        ).append(
                buildGaps(gaps)
        );

        result.append(
                "\n\nDAYS 1-30 — FOUNDATION\n"
        );

        appendTasks(
                result,
                buildPhaseOne(profile, gaps)
        );

        result.append(
                "\nDAYS 31-60 — PROJECT & DEPTH\n"
        );

        appendTasks(
                result,
                buildPhaseTwo(profile, gaps)
        );

        result.append(
                "\nDAYS 61-90 — JOB READINESS\n"
        );

        appendTasks(
                result,
                buildPhaseThree(profile)
        );

        result.append(
                "\n90-DAY OUTCOME:\n"
        );

        result.append(
                "By day 90, you should have a stronger "
                        + profile.name()
                        + " profile for "
                        + goal
                        + ", supported by stronger technical depth, "
                        + "a focused project, clearer resume evidence, "
                        + "interview preparation, and an application-ready portfolio."
        );

        return result
                .toString()
                .trim();
    }

    public String generate(
            String careerGoal,
            String sessionId) {

        return generateRoadmap(
                careerGoal,
                sessionId
        );
    }

    private RoleProfile selectProfile(
            String goal) {

        String lower =
                goal.toLowerCase(Locale.ROOT);

        if (lower.contains("data engineer")
                || lower.contains("data engineering")) {

            return new RoleProfile(
                    "Data Engineer",
                    List.of(
                            "SQL",
                            "Python",
                            "PySpark",
                            "Azure",
                            "Azure Data Factory",
                            "Databricks",
                            "Synapse Analytics",
                            "Microsoft Fabric",
                            "ETL",
                            "Data Engineering",
                            "Data Governance",
                            "Power BI",
                            "Git",
                            "Projects"
                    )
            );
        }

        if (lower.contains("backend")
                || lower.contains("java")) {

            return new RoleProfile(
                    "Java Backend Developer",
                    List.of(
                            "Core Java",
                            "Spring Boot",
                            "REST APIs",
                            "SQL",
                            "Spring Data JPA",
                            "Testing",
                            "Docker",
                            "Deployment"
                    )
            );
        }

        if (lower.contains("full stack")
                || lower.contains("fullstack")) {

            return new RoleProfile(
                    "Full Stack Developer",
                    List.of(
                            "Java",
                            "Spring Boot",
                            "REST APIs",
                            "SQL",
                            "JavaScript",
                            "React.js",
                            "Git",
                            "Deployment"
                    )
            );
        }

        if (lower.contains("data analyst")
                || lower.contains("analyst")) {

            return new RoleProfile(
                    "Data Analyst",
                    List.of(
                            "SQL",
                            "Excel",
                            "Python",
                            "Data Visualization",
                            "Statistics",
                            "Power BI",
                            "Projects"
                    )
            );
        }

        if (lower.contains("machine learning")
                || lower.contains("ml engineer")) {

            return new RoleProfile(
                    "Machine Learning Engineer",
                    List.of(
                            "Python",
                            "SQL",
                            "Machine Learning",
                            "Scikit-learn",
                            "Model Evaluation",
                            "APIs",
                            "Deployment",
                            "Projects"
                    )
            );
        }

        return new RoleProfile(
                goal,
                List.of(
                        "Role fundamentals",
                        "Hands-on project",
                        "Problem solving",
                        "Testing",
                        "Git",
                        "Deployment",
                        "Resume evidence",
                        "Interview preparation"
                )
        );
    }

    private List<String> findGaps(
            List<String> focusSkills,
            String resume) {

        String normalized =
                normalize(resume);

        List<String> gaps =
                new ArrayList<>();

        for (String skill : focusSkills) {

            String key =
                    normalize(skill);

            if (key.equals("projects")
                    || key.equals("hands on project")
                    || key.equals("resume evidence")
                    || key.equals("problem solving")
                    || key.equals("role fundamentals")
                    || key.equals("interview preparation")) {

                continue;
            }

            if (key.equals("data visualization")) {

                if (!normalized.contains("power bi")
                        && !normalized.contains("tableau")
                        && !normalized.contains("visualization")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("testing")) {

                if (!normalized.contains("junit")
                        && !normalized.contains("testing")
                        && !normalized.contains("unit test")
                        && !normalized.contains("test cases")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("deployment")) {

                if (!normalized.contains("deployed")
                        && !normalized.contains("deployment")
                        && !normalized.contains("railway")
                        && !normalized.contains("render")
                        && !normalized.contains("aws")
                        && !normalized.contains("azure")
                        && !normalized.contains("docker")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("etl")) {

                if (!normalized.contains("etl")
                        && !normalized.contains("extract transform load")
                        && !normalized.contains("data pipeline")
                        && !normalized.contains("pipeline")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("data engineering")) {

                if (!normalized.contains("data engineer")
                        && !normalized.contains("data engineering")
                        && !normalized.contains("data pipeline")
                        && !normalized.contains("etl")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("data governance")) {

                if (!normalized.contains("data governance")
                        && !normalized.contains("governance")
                        && !normalized.contains("data quality")
                        && !normalized.contains("unity catalog")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("azure data factory")) {

                if (!normalized.contains("azure data factory")
                        && !normalized.contains("adf")
                        && !normalized.contains("data factory")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("synapse analytics")) {

                if (!normalized.contains("synapse analytics")
                        && !normalized.contains("synapse")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("microsoft fabric")) {

                if (!normalized.contains("microsoft fabric")
                        && !normalized.contains("fabric")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("power bi")) {

                if (!normalized.contains("power bi")
                        && !normalized.contains("powerbi")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("pyspark")) {

                if (!normalized.contains("pyspark")
                        && !normalized.contains("spark")
                        && !normalized.contains("spark sql")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("databricks")) {

                if (!normalized.contains("databricks")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("azure")) {

                if (!normalized.contains("azure")
                        && !normalized.contains("microsoft azure")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("python")) {

                if (!normalized.contains("python")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("sql")) {

                if (!normalized.contains("sql")
                        && !normalized.contains("mysql")
                        && !normalized.contains("postgresql")
                        && !normalized.contains("sql server")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("git")) {

                if (!normalized.contains("git")
                        && !normalized.contains("github")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("core java")
                    || key.equals("java")) {

                if (!normalized.contains("java")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("spring boot")) {

                if (!normalized.contains("spring boot")
                        && !normalized.contains("springboot")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("rest apis")
                    || key.equals("rest api")) {

                if (!normalized.contains("rest api")
                        && !normalized.contains("restful")
                        && !normalized.contains("api")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("spring data jpa")) {

                if (!normalized.contains("spring data jpa")
                        && !normalized.contains("jpa")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("docker")) {

                if (!normalized.contains("docker")
                        && !normalized.contains("container")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("javascript")) {

                if (!normalized.contains("javascript")
                        && !normalized.contains("java script")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("react js")
                    || key.equals("react.js")) {

                if (!normalized.contains("react js")
                        && !normalized.contains("react")
                        && !normalized.contains("reactjs")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("excel")) {

                if (!normalized.contains("excel")
                        && !normalized.contains("microsoft excel")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("statistics")) {

                if (!normalized.contains("statistics")
                        && !normalized.contains("statistical")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("machine learning")) {

                if (!normalized.contains("machine learning")
                        && !normalized.contains("machine-learning")
                        && !normalized.contains("ml")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("scikit learn")) {

                if (!normalized.contains("scikit")
                        && !normalized.contains("sklearn")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("model evaluation")) {

                if (!normalized.contains("model evaluation")
                        && !normalized.contains("model validation")
                        && !normalized.contains("evaluation")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (key.equals("apis")) {

                if (!normalized.contains("api")
                        && !normalized.contains("apis")) {

                    gaps.add(skill);
                }

                continue;
            }

            if (!normalized.contains(key)) {
                gaps.add(skill);
            }
        }

        return gaps;
    }

    private String buildCurrentProfile(
            String resume,
            RoleProfile profile) {

        String normalized =
                normalize(resume);

        List<String> detected =
                new ArrayList<>();

        for (String skill : profile.focusSkills()) {

            String key =
                    normalize(skill);

            boolean present = false;

            if (key.equals("projects")
                    || key.equals("hands on project")) {

                present =
                        normalized.contains("project")
                                || normalized.contains("projects");

            } else if (key.equals("data engineering")) {

                present =
                        normalized.contains("data engineer")
                                || normalized.contains("data engineering")
                                || normalized.contains("data pipeline")
                                || normalized.contains("etl");

            } else if (key.equals("etl")) {

                present =
                        normalized.contains("etl")
                                || normalized.contains("pipeline")
                                || normalized.contains("data transformation");

            } else if (key.equals("data governance")) {

                present =
                        normalized.contains("data governance")
                                || normalized.contains("governance")
                                || normalized.contains("unity catalog");

            } else if (key.equals("azure data factory")) {

                present =
                        normalized.contains("azure data factory")
                                || normalized.contains("adf")
                                || normalized.contains("data factory");

            } else if (key.equals("synapse analytics")) {

                present =
                        normalized.contains("synapse analytics")
                                || normalized.contains("synapse");

            } else if (key.equals("microsoft fabric")) {

                present =
                        normalized.contains("microsoft fabric")
                                || normalized.contains("fabric");

            } else if (key.equals("power bi")) {

                present =
                        normalized.contains("power bi")
                                || normalized.contains("powerbi");

            } else if (key.equals("pyspark")) {

                present =
                        normalized.contains("pyspark")
                                || normalized.contains("spark");

            } else if (key.equals("rest apis")
                    || key.equals("rest api")) {

                present =
                        normalized.contains("rest api")
                                || normalized.contains("restful")
                                || normalized.contains("api");

            } else if (key.equals("spring data jpa")) {

                present =
                        normalized.contains("spring data jpa")
                                || normalized.contains("jpa");

            } else if (key.equals("data visualization")) {

                present =
                        normalized.contains("power bi")
                                || normalized.contains("tableau")
                                || normalized.contains("visualization");

            } else if (key.equals("machine learning")) {

                present =
                        normalized.contains("machine learning")
                                || normalized.contains("ml");

            } else if (key.equals("scikit learn")) {

                present =
                        normalized.contains("scikit")
                                || normalized.contains("sklearn");

            } else if (key.equals("model evaluation")) {

                present =
                        normalized.contains("model evaluation")
                                || normalized.contains("model validation")
                                || normalized.contains("evaluation");

            } else if (key.equals("core java")) {

                present =
                        normalized.contains("java");

            } else {

                present =
                        normalized.contains(key);
            }

            if (present) {
                detected.add(skill);
            }

            if (detected.size() == 8) {
                break;
            }
        }

        if (detected.isEmpty()) {

            return "Early-career technical profile based on the uploaded resume.";
        }

        return "Current strengths visible in the resume: "
                + String.join(", ", detected)
                + ".";
    }

    private String buildGaps(
            List<String> gaps) {

        if (gaps.isEmpty()) {

            return "No major technical gaps were identified for the selected target profile.";
        }

        return "Priority areas to strengthen: "
                + String.join(", ", gaps)
                + ".";
    }

    private List<String> buildPhaseOne(
            RoleProfile profile,
            List<String> gaps) {

        List<String> tasks =
                new ArrayList<>();

        if (gaps.isEmpty()) {

            tasks.add(
                    "Review and strengthen the core "
                            + profile.name()
                            + " concepts already present in your resume."
            );

            tasks.add(
                    "Solve focused technical exercises at least 4 days per week and track weak topics."
            );

            tasks.add(
                    "Review your resume and identify the strongest project and professional evidence for the target role."
            );

        } else {

            String firstGap =
                    gaps.get(0);

            tasks.add(
                    "Strengthen "
                            + firstGap
                            + " through focused study and hands-on exercises."
            );

            if (gaps.size() > 1) {

                tasks.add(
                        "Build working knowledge of "
                                + gaps.get(1)
                                + " and complete at least one practical exercise."
                );

            } else {

                tasks.add(
                        "Practice the existing "
                                + profile.name()
                                + " skills through role-specific technical exercises."
                );
            }

            tasks.add(
                    "Review your resume and connect each major skill to concrete project or professional evidence."
            );
        }

        return tasks;
    }

    private List<String> buildPhaseTwo(
            RoleProfile profile,
            List<String> gaps) {

        List<String> tasks =
                new ArrayList<>();

        tasks.add(
                "Build or extend one "
                        + profile.name()
                        + " project using the strongest technologies already present in the resume."
        );

        if (!gaps.isEmpty()) {

            String focusGap =
                    gaps.size() > 1
                            ? gaps.get(1)
                            : gaps.get(0);

            tasks.add(
                    "Add "
                            + focusGap
                            + " to the project through a practical implementation."
            );

        } else {

            tasks.add(
                    "Increase project depth by adding better data flow, validation, error handling, testing, monitoring or performance improvements where relevant."
            );
        }

        tasks.add(
                "Document architecture, technical decisions, measurable outcomes and the candidate's individual contribution."
        );

        return tasks;
    }

    private List<String> buildPhaseThree(
            RoleProfile profile) {

        List<String> tasks =
                new ArrayList<>();

        tasks.add(
                "Finish the strongest project for the "
                        + profile.name()
                        + " target and verify the complete workflow."
        );

        if (profile.name().equalsIgnoreCase("Data Engineer")) {

            tasks.add(
                    "Practice SQL, Python/PySpark, ETL pipelines, cloud data services, data transformation and troubleshooting interview questions."
            );

        } else if (profile.name().equalsIgnoreCase("Java Backend Developer")) {

            tasks.add(
                    "Practice Core Java, Spring Boot, REST APIs, SQL, debugging, testing and backend system design questions."
            );

        } else if (profile.name().equalsIgnoreCase("Data Analyst")) {

            tasks.add(
                    "Practice SQL, Python, Excel, Power BI, statistics, data interpretation and case-based interview questions."
            );

        } else {

            tasks.add(
                    "Practice technical interviews covering core concepts, project decisions, debugging and role-specific questions."
            );
        }

        tasks.add(
                "Tailor the resume and applications for the target role using only truthful keywords and evidence already supported by the resume."
        );

        return tasks;
    }

    private void appendTasks(
            StringBuilder result,
            List<String> tasks) {

        int number = 1;

        for (String task : tasks) {

            result.append(number++)
                    .append(". ")
                    .append(task)
                    .append("\n");
        }
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace("restful", "rest")
                .replace("reactjs", "react js")
                .replace("powerbi", "power bi")
                .replace("microsoftazure", "microsoft azure")
                .replaceAll("[^a-z0-9+#]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record RoleProfile(
            String name,
            List<String> focusSkills) {
    }
}
# 🤖 AI Career Copilot

> An AI-powered career assistant built with Java, Spring Boot, Spring AI, RAG, Ollama, and PostgreSQL + PGVector to help users analyze resumes, match jobs, identify skill gaps, practice interviews, and build a focused career roadmap.

<p align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-green?style=for-the-badge&logo=springboot)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.1-blue?style=for-the-badge&logo=spring)
![Ollama](https://img.shields.io/badge/Ollama-Local%20AI-black?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql)
![PGVector](https://img.shields.io/badge/PGVector-Vector%20Search-purple?style=for-the-badge)

</p>

---

## ✨ Overview

**AI Career Copilot** is a Java and Spring Boot based career intelligence application that turns a resume into an interactive AI-powered career assistant.

Users can upload a PDF resume and use a single workspace to:

- 📄 Analyze their resume
- 💬 Ask questions about their resume
- 🎯 Match their resume against a job description
- 🧠 Identify skill gaps
- 🎤 Practice technical interviews
- 🗺️ Generate a personalized 90-day career roadmap

The application combines **Spring Boot, Spring AI, Retrieval-Augmented Generation (RAG), Ollama, PostgreSQL, and PGVector** to create a practical AI-assisted career workflow.

---

# 🚀 Features

## 📄 Resume Ingestion

Upload a PDF resume and process it into searchable document chunks.

The application:

- Accepts PDF resumes
- Extracts resume content
- Splits content into chunks
- Generates vector embeddings
- Stores embeddings in PostgreSQL + PGVector
- Maintains an active resume context for the current session

---

## 💬 Resume Chat

Ask natural-language questions about the uploaded resume.

Example questions:

- What are my strongest technical skills?
- What backend technologies do I know?
- What projects have I worked on?
- Which technologies in my resume are relevant to Java backend development?

The Resume Chat feature uses **Retrieval-Augmented Generation (RAG)** to retrieve relevant resume context before generating the response.

---

## 📊 Resume Analyzer

Generates a structured analysis of the uploaded resume.

The analyzer provides:

- Role/profile identification
- Overall resume summary
- Strengths
- Areas to improve
- Weak or missing areas
- ATS-related observations
- Recommended next steps

---

## 🎯 Job Matcher

Compare the uploaded resume with a target job description.

The matcher identifies:

- Overall match percentage
- Matching skills
- Missing or weak skills
- Relevant resume experience
- Match score
- Recommendations for improvement

Example:

```text
OVERALL MATCH: 93%

MATCHING SKILLS:
Java
Spring Boot
Spring Security
Spring Data JPA
Hibernate
REST APIs
PostgreSQL
Docker
Git

MISSING OR WEAK SKILLS:
Microservices

MATCH SCORE:
93/100
```

---

## 🧠 Skill Gap Analyzer

Analyze a target job description against the uploaded resume.

It identifies:

- Skills already present
- Skills that need improvement
- Missing skills
- Learning priorities
- Practical learning recommendations

This helps turn a job description into a structured learning plan.

---

## 🎤 AI Interview

Practice technical interview questions based on a selected topic.

Example topics:

- Java
- Spring Boot
- SQL
- Spring Security
- Backend Development

The workflow is:

```text
Select Topic
      ↓
Generate Interview Question
      ↓
Candidate Answers
      ↓
Evaluate Answer
      ↓
Score + Feedback
      ↓
Improvement Suggestions
```

The interview evaluation provides:

- Score
- Feedback
- Improvement areas
- Technical guidance

---

## 🗺️ Career Roadmap

Generate a role-focused **90-day career roadmap** based on the user's profile and career goal.

The roadmap is organized into:

### Days 1–30

Foundation and skill strengthening

### Days 31–60

Project development and technical depth

### Days 61–90

Job readiness and interview preparation

The roadmap also highlights priority areas that should be strengthened for the target role.

---

# 🧠 RAG Architecture

The Resume Chat workflow follows a Retrieval-Augmented Generation architecture.

```text
                ┌──────────────────────┐
                │    Upload Resume     │
                │        PDF           │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Document Ingestion   │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │    Text Chunking     │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │   Embedding Model    │
                │  nomic-embed-text    │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ PostgreSQL + PGVector│
                └──────────┬───────────┘
                           │
                    User Question
                           │
                           ▼
                ┌──────────────────────┐
                │  Vector Retrieval    │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │     Ollama LLM       │
                │      llama3.2        │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │  Grounded Response   │
                └──────────────────────┘
```

---

# 🏗️ Project Architecture

```text
AI-Career-Copilot/
│
├── .mvn/
│   └── wrapper/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hr/AICareerCopilot/
│   │   │
│   │   │       ├── config/
│   │   │       │   └── RagProperties.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── CareerRoadmapController.java
│   │   │       │   ├── ChatController.java
│   │   │       │   ├── IngestionController.java
│   │   │       │   ├── InterviewController.java
│   │   │       │   ├── JobDescriptionMatcherController.java
│   │   │       │   ├── ResumeAnalyzerController.java
│   │   │       │   └── SkillGapAnalyzerController.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── CareerRoadmapRequest.java
│   │   │       │   ├── ChatRequest.java
│   │   │       │   ├── InterviewRequest.java
│   │   │       │   ├── JobMatchRequest.java
│   │   │       │   └── SkillGapRequest.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   └── Resume.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   └── ResumeRepository.java
│   │   │       │
│   │   │       └── service/
│   │   │           ├── ActiveResumeService.java
│   │   │           ├── CareerRoadmapService.java
│   │   │           ├── DocumentIngestionService.java
│   │   │           ├── InterviewService.java
│   │   │           ├── JobDescriptionMatcherService.java
│   │   │           ├── ResumeAnalyzerService.java
│   │   │           ├── ResumeContextService.java
│   │   │           └── SkillGapAnalyzerService.java
│   │   │
│   │   └── resources/
│   │       ├── documents/
│   │       │   └── resume.pdf
│   │       │
│   │       ├── static/
│   │       │   └── index.html
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│
├── compose.yaml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

# 🛠️ Tech Stack

## Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring AI

## AI / RAG

- Ollama
- Llama 3.2
- Nomic Embed Text
- Retrieval-Augmented Generation
- Vector similarity search

## Database

- PostgreSQL
- PGVector

## Frontend

- HTML
- CSS
- JavaScript

## Infrastructure

- Docker Compose
- Maven

---

# 🤖 AI Models

### Chat / Generation Model

`llama3.2:latest`

### Embedding Model

`nomic-embed-text`

The AI workflow is designed around local Ollama models for development and local inference.

---

# 🔌 API Endpoints

| Feature | Method | Endpoint |
|---|---|---|
| Resume Upload | POST | `/api/ingestion/resume` |
| Resume Chat | POST | `/api/chat` |
| Resume Analyzer | GET | `/api/resume/analyze` |
| Job Matcher | POST | `/api/job/match` |
| Skill Gap | POST | `/api/skill-gap/analyze` |
| Interview Question | POST | `/api/interview/question` |
| Interview Evaluation | POST | `/api/interview/evaluate` |
| Career Roadmap | POST | `/api/career/roadmap` |

---

# ⚙️ Prerequisites

Before running the application, install:

- Java 21+
- Maven
- Docker
- Ollama

---

# 📦 Setup & Installation

## 1. Clone the Repository

```bash
git clone https://github.com/KritikaaSinghh/AI-Career-Copilot.git
cd AI-Career-Copilot
```

## 2. Start PostgreSQL + PGVector

```bash
docker compose up -d
```

The application uses:

```text
Host: localhost
Port: 5433
Database: mydatabase
Username: myuser
```

## 3. Start Ollama

Make sure Ollama is installed and running.

Check installed models:

```bash
ollama list
```

Pull the required models:

```bash
ollama pull llama3.2:latest
ollama pull nomic-embed-text
```

## 4. Run the Application

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

The application can also be started from IntelliJ IDEA.

## 5. Open the Application

```text
http://localhost:8080
```

---

# 🔄 Application Workflow

```text
Upload Resume
      ↓
PDF Text Extraction
      ↓
Document Chunking
      ↓
Embedding Generation
      ↓
PGVector Storage
      ↓
Active Resume Context
      ↓
AI Career Features
      ├── Resume Chat
      ├── Resume Analyzer
      ├── Job Matcher
      ├── Skill Gap
      ├── AI Interview
      └── Career Roadmap
```

---

# 🧪 Testing

The main application flows have been tested successfully:

- ✅ Resume Upload
- ✅ Resume Analyzer
- ✅ Resume Chat / RAG
- ✅ Job Matcher
- ✅ Skill Gap Analysis
- ✅ Interview Question Generation
- ✅ Interview Evaluation
- ✅ Career Roadmap Generation

---

# 💡 Example Use Case

A candidate wants to apply for a Java Backend Developer role.

The application supports the following workflow:

```text
1. Upload Resume
        ↓
2. Analyze Resume
        ↓
3. Paste Job Description
        ↓
4. Check Job Match
        ↓
5. Analyze Skill Gap
        ↓
6. Practice Spring Boot Interview
        ↓
7. Generate 90-Day Career Roadmap
```

This creates a single career workflow from:

**Resume Analysis → Job Matching → Skill Gap → Learning → Interview Preparation → Career Planning**

---

# 🎯 What This Project Demonstrates

This project demonstrates practical experience with:

- Java backend development
- Spring Boot REST APIs
- Spring Data JPA
- Hibernate
- PostgreSQL
- PGVector
- Vector embeddings
- Retrieval-Augmented Generation
- Local LLM integration with Ollama
- PDF document ingestion
- Session-based active resume context
- Docker Compose
- Frontend-backend integration
- AI-assisted career workflows

---

# 🔐 Security & Configuration

The project is intended primarily for local development.

Do not commit:

- API keys
- Production passwords
- Private credentials
- Personal documents
- Environment-specific secrets

For production deployments, secrets should be supplied through environment variables or a secure secrets manager.

---

# 🚀 Future Improvements

Potential future enhancements include:

- Real-time job search integration
- Job recommendation system
- Resume version management
- Multiple resume profiles
- Advanced ATS scoring
- More conversational interview sessions
- Interview history and performance tracking
- User authentication and accounts
- Analytics dashboard
- Dockerized full application deployment
- Cloud-hosted PostgreSQL and vector search

---

# 👩‍💻 Author

## Kritika Singh

**Java Backend Developer | Spring Boot | AI | RAG**

GitHub: https://github.com/KritikaaSinghh

Project Repository: https://github.com/KritikaaSinghh/AI-Career-Copilot

---

# ⭐ Support

If you find this project useful, consider giving the repository a ⭐ on GitHub.

---

<p align="center">

### Built with ☕ Java + Spring Boot + 🤖 AI + 🧠 RAG

</p>

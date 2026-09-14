# 🤖 AI Career Copilot

> An AI-powered career assistant built with **Java, Spring Boot, RAG, Ollama, and PostgreSQL + PGVector** to help users analyze resumes, match jobs, identify skill gaps, practice interviews, and build a focused career roadmap.

<p align="center">

  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java"/>

  <img src="https://img.shields.io/badge/Spring%20Boot-4-green?style=for-the-badge&logo=springboot" alt="Spring Boot"/>

  <img src="https://img.shields.io/badge/Spring%20AI-2.0.1-blue?style=for-the-badge&logo=spring" alt="Spring AI"/>

  <img src="https://img.shields.io/badge/Ollama-Local%20AI-black?style=for-the-badge" alt="Ollama"/>

  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql" alt="PostgreSQL"/>

  <img src="https://img.shields.io/badge/PGVector-Vector%20Search-purple?style=for-the-badge" alt="PGVector"/>

</p>

---

## ✨ Overview

**AI Career Copilot** is a full-stack Java-based career intelligence application that turns a resume into an interactive career assistant.

Users can upload a PDF resume and use a single workspace to:

- 📄 Analyze their resume
- 💬 Ask questions about their resume
- 🎯 Match their resume against a job description
- 🧠 Identify skill gaps
- 🎤 Practice technical interviews
- 🗺️ Generate a personalized 90-day career roadmap

The application combines **Spring Boot**, **Spring AI**, **RAG**, **Ollama**, and **PostgreSQL with PGVector** to create a local AI-powered career workflow.

---

# 🚀 Features

## 📄 Resume Ingestion

Upload a PDF resume and process it into searchable document chunks.

The application:

- Accepts PDF resumes
- Extracts resume content
- Splits the content into chunks
- Stores vector representations
- Tracks the active resume for the current session

---

## 💬 Resume Chat

Ask natural-language questions about the uploaded resume.

Example questions:

```text
What are my strongest technical skills?

What backend technologies do I know?

What projects have I worked on?

Which technologies mentioned in my resume are related to Java backend development?

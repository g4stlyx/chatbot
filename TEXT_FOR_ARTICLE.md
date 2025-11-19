# Abstract

This paper presents the design and implementation of a secure, full-stack chatbot application engineered to interface with local Large Language Models (LLMs), specifically utilizing Ollama to run Llama 3. Addressing the growing demand for privacy-focused and customizable AI solutions, the system is built upon a robust microservices-inspired architecture using Spring Boot (Java 17) for the RESTful backend and React (Vite) for a responsive frontend.

Data persistence and system performance are optimized through a hybrid database strategy: MySQL is employed for structured relational data—including user profiles, chat sessions, and message history—while Redis is utilized for high-speed caching and session management via Docker containerization. Security is a cornerstone of the architecture, featuring a comprehensive authentication suite that implements JWT (JSON Web Tokens) for stateless authorization and Argon2 hashing with salt and pepper mechanisms for secure credential storage.

The initial phase of development focuses on core lifecycle and interaction capabilities. This includes a complete identity management system (registration, email verification, password recovery), dynamic chat session control (CRUD operations, session archiving/pausing), and real-time message delivery using Server-Sent Events (SSE) for streaming LLM responses. The frontend architecture leverages the Context API for global state management and integrates Axios interceptors for secure HTTP communication, providing a seamless user experience with support for Markdown rendering and code syntax highlighting. This project demonstrates a scalable, secure foundation for deploying local AI applications that prioritize data sovereignty without compromising on modern web application standards.

## Keywords: 
Local LLM, Secure Chatbot, Spring Boot, React, Ollama, JWT Authentication, Redis Caching, Server-Sent Events (SSE), Data Privacy, Full-stack Architecture.

# 1. Introduction

The rapid advancement of Large Language Models (LLMs) has democratized access to artificial intelligence, fundamentally altering how users interact with software systems. While cloud-based solutions dominate the current market, they present significant challenges regarding data sovereignty, privacy compliance, and recurring operational costs. For enterprises and privacy-conscious individuals, the requirement to transmit sensitive data to external third-party servers is often a critical barrier to adoption. Consequently, there is a growing demand for robust, self-hosted alternatives that can leverage open-source models without compromising on the user experience or security standards expected of modern web applications.

This study presents the design and implementation of a secure, full-stack chatbot application tailored for local LLM inference. Built upon a modern technology stack comprising Java Spring Boot and React, the system addresses the complexities of integrating stateful web sessions with stateless AI model interactions. The project utilizes Ollama to host Llama 3 locally, ensuring that all inference occurs within the deployment environment, thereby guaranteeing that sensitive user data is never exposed to external API providers.

A primary focus of the initial development phase (Part 1) was establishing a secure and scalable foundation. The backend architecture implements rigorous security protocols, utilizing Argon2 hashing with salt and pepper mechanisms for credential storage to thwart rainbow table attacks, and JSON Web Tokens (JWT) for stateless, scalable authorization. We address common web vulnerabilities through strictly configured CORS policies and secure endpoint protection, ensuring that critical features such as chat history and profile management are accessible only to authenticated users. The system also includes a comprehensive identity management suite, handling registration, email verification, and password recovery flows.

On the client side, the application leverages React with Vite to deliver a high-performance Single Page Application (SPA). Unlike basic command-line interfaces often associated with local LLMs, this system provides a rich user experience comparable to commercial SaaS platforms. Key features implemented in this phase include real-time response streaming via Server-Sent Events (SSE), robust global state management using the Context API, and secure HTTP communication via Axios interceptors which handle token lifecycle management automatically. The interface is designed for technical depth, supporting complex interactions such as Markdown rendering and code syntax highlighting.

Underpinning this application is a containerized infrastructure orchestrated via Docker. The system employs a hybrid database strategy: MySQL is utilized for persistent, structured data—including user profiles and chat session history—while Redis is deployed for high-speed caching and session management. This paper details the architectural decisions and implementation strategies of these core features, demonstrating how modern web technologies can be effectively coupled with local AI models to create secure, private, and scalable intelligent applications.

# 2. Related Work

# 3. Materials and Methods

# 4. Results

# 5. Conclusion

# 6. Open Issues & Future Works

# References
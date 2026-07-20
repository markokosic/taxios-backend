# 3. OpenAPI Contract and Frontend Code Generation

## Status
Accepted

## Context
Our project consists of a Spring Boot backend and a TypeScript-based frontend. As the project grows, manually writing, updating, and maintaining frontend API clients, TypeScript interfaces, and validation schemas (e.g., Zod) to match the backend DTOs introduces several risks and inefficiencies:
1. **Contract Drift:** Changes in backend DTOs (e.g., field renaming, type changes) can silently break the frontend without compile-time errors.
2. **Boilerplate Code:** Developers have to write repetitive fetching code (using Axios/Fetch) and state management wrappers (e.g., TanStack Query).
3. **Manual Validation:** Validation schemas for forms (e.g., Zod) must be written and synchronized manually with backend Jakarta Validation constraints.

We needed to decide on a strategy for API contract definition and automated client generation.

We evaluated two main approaches:
1. **Design-First (Spec-First):** Writing the OpenAPI YAML manually first, then generating both Spring Boot controller interfaces/DTOs and frontend clients.
2. **Code-First with Automatic Frontend Generation:** Writing standard Spring Boot controllers annotated with Springdoc OpenAPI annotations, generating the OpenAPI YAML from the running/built backend, and using a frontend generator (Orval) to produce TypeScript types, Zod schemas, and TanStack Query hooks.

## Decision
We will adopt the **Code-First OpenAPI generation approach using Springdoc OpenAPI on the backend and Orval on the frontend**.

Springdoc OpenAPI is already integrated into our backend to inspect controllers and produce OpenAPI specs. We will extract this spec and feed it into Orval on the frontend to automatically generate:
* Fully typed API clients (using Axios/Fetch)
* TanStack Query hooks (e.g., `useQuery`, `useMutation`) matching each endpoint
* Zod schemas corresponding to our request DTOs for form validation

This maintains a fast development loop in Spring Boot while providing complete end-to-end type safety.

## Consequences
* **Positive:** **End-to-End Type Safety:** Any breaking change in a backend DTO immediately triggers a TypeScript compilation error in the frontend upon regeneration.
* **Positive:** **Zero Fetching Boilerplate:** Developers do not need to write API clients or react-query wrappers manually. They are generated instantly.
* **Positive:** **Synchronized Validation:** Zod schemas are generated directly from the OpenAPI definition, reflecting the validation rules defined in the backend.
* **Negative:** **Build-Time Dependency:** The frontend client generation requires an updated OpenAPI spec file, which requires either running the backend or executing a Maven plugin (`springdoc-openapi-maven-plugin`) during the build process.

# 2. Multi-Tenancy Isolation & Registration Atomicity

## Status
Accepted

## Context
In our application, we utilize a multi-tenancy system based on a shared database and shared schema with row-level isolation via the Hibernate `@TenantId` annotation.

Additionally, we have globally disabled Open-Session-In-View (OSIV) (`spring.jpa.open-in-view=false`) to avoid performance bottlenecks and connection leaks.

This combination introduced a conflict during the registration of a new tenant (Tenant + Admin User):
* Hibernate sessions are bound to a fixed tenant ID that is resolved when the session opens. Therefore, at the beginning of the registration request, the session is locked to ID `0` (or `null`).
* However, the newly created tenant receives a dynamically generated ID (e.g., `7`).
* Attempting to save the new user with ID `7` within the same session/transaction via JPA (`save()`) is blocked by Hibernate with a `PropertyValueException`, because the user's tenant ID does not match the active session tenant ID (`0`).

## Decision
We will ensure the registration process is wrapped inside a **single, atomic transaction** to guarantee data consistency.

To resolve the Hibernate session conflict, we will use a **native SQL query** (`nativeQuery = true`) within the `UserRepository` for the initial user insert.

We reject splitting the registration into multiple transactions, as this would violate ACID principles and could result in "orphaned" tenants in the database if an error occurs mid-process.

## Consequences
* **Positive:** The registration process is completely atomic. If the user creation fails, the tenant creation is rolled back automatically.
* **Positive:** We maintain the performance benefits of `spring.jpa.open-in-view=false` throughout the rest of the system.
* **Neutral:** For this one-time registration insert, we bypass the JPA entity lifecycle management (e.g., `@PrePersist` or lifecycle listeners are not triggered for this specific insert). Since the user does not require complex lifecycle hooks during registration, this is an acceptable trade-off.
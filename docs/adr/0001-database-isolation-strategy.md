# 1. Multi-Tenancy Database Isolation Strategy

## Status
Accepted

## Context
Our application (minicrm) is a SaaS platform operated for multiple independent tenants. Strict data isolation is business-critical. We needed to decide on a strategy for isolating tenant data.

We evaluated three approaches:
1. Physically separate databases per tenant (Database-per-Tenant)
2. Logically separate schemas within a shared database (Schema-per-Tenant)
3. Shared database and shared schema with a discriminator column (Shared Database, Shared Schema)

## Decision
We will implement the **Shared Database, Shared Schema with row-level isolation via a discriminator column (`tenant_id`)** approach.

For our CRM platform, this approach offers the best combination of cost efficiency (low resource consumption) and easy maintainability (unified database schemas and migration scripts via Liquibase).

The risk of accidental cross-tenant data exposure is minimized at the application level through automated filtering (see ADR 2) and at the database level via foreign keys.

## Consequences
* **Positive:** Very low hosting and operational costs, as we only need to run a single relational database instance.
* **Positive:** Database migrations (e.g., table changes) are applied via Liquibase in a single step for all tenants simultaneously.
* **Negative:** Flaws in the application logic could theoretically lead to data leaks. We must rely heavily on automated framework filters (such as `@TenantId` in JPA) to prevent human developer error.
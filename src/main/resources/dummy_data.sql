-- Seed ~200 tasks using existing employees (IDs 1..5)
SET search_path TO to_do_list, public;

WITH seed AS (
    SELECT
        gs AS n,
        CASE (gs - 1) % 3
            WHEN 0 THEN 'FrontEnd'
            WHEN 1 THEN 'BackEnd'
            ELSE 'IaC'
        END AS stream,
        CASE (gs - 1) % 5
            WHEN 0 THEN 'BACK_LOG'
            WHEN 1 THEN 'IN_PROGRESS'
            WHEN 2 THEN 'TESTING'
            WHEN 3 THEN 'COMPLETED'
            ELSE 'DEPLOYED_TO_PRODUCTION'
        END AS status,
        1 + ((gs - 1) % 5) AS assignee,
        1 + (gs % 5) AS reporter,
        TIMESTAMP '2026-01-01 09:00:00' + ((gs - 1) % 180) * INTERVAL '1 day' AS create_date
    FROM generate_series(1, 200) gs
),
descriptions AS (
    SELECT
        n,
        stream,
        status,
        assignee,
        reporter,
        create_date,
        CASE stream
            WHEN 'FrontEnd' THEN
                CASE (n - 1) % 10
                    WHEN 0 THEN format('Implement responsive dashboard cards for sprint metrics, including loading and empty states. [Ticket %s]', n)
                    WHEN 1 THEN format('Refactor React form validation for task creation and improve error messaging UX. [Ticket %s]', n)
                    WHEN 2 THEN format('Build reusable modal for task assignment with keyboard accessibility support. [Ticket %s]', n)
                    WHEN 3 THEN format('Integrate task status timeline component with API data and fallback placeholders. [Ticket %s]', n)
                    WHEN 4 THEN format('Optimize bundle splitting for project board page and reduce first contentful paint. [Ticket %s]', n)
                    WHEN 5 THEN format('Add Cypress coverage for kanban drag-and-drop edge cases across breakpoints. [Ticket %s]', n)
                    WHEN 6 THEN format('Create dark-theme tokens for task detail panel and align with design system. [Ticket %s]', n)
                    WHEN 7 THEN format('Fix pagination state reset on filtered task list after route navigation. [Ticket %s]', n)
                    WHEN 8 THEN format('Implement optimistic UI update for task comments with rollback on failure. [Ticket %s]', n)
                    ELSE format('Add localization support for task status labels and date formatting rules. [Ticket %s]', n)
                END
            WHEN 'BackEnd' THEN
                CASE (n - 1) % 10
                    WHEN 0 THEN format('Expose REST endpoint to bulk update task statuses with audit logging and validation. [Ticket %s]', n)
                    WHEN 1 THEN format('Improve JPA query performance for overdue task reporting with proper indexing hints. [Ticket %s]', n)
                    WHEN 2 THEN format('Add service-layer retry policy for transient mail notification failures via queue consumer. [Ticket %s]', n)
                    WHEN 3 THEN format('Implement task SLA evaluator job and persist breached thresholds for analytics. [Ticket %s]', n)
                    WHEN 4 THEN format('Harden API authorization checks for reporter/assignee mutations on task resources. [Ticket %s]', n)
                    WHEN 5 THEN format('Refactor domain mapping between DTO and entity to reduce duplicate conversion logic. [Ticket %s]', n)
                    WHEN 6 THEN format('Add OpenAPI documentation for task filtering, sorting, and pagination contracts. [Ticket %s]', n)
                    WHEN 7 THEN format('Implement integration test fixture for task lifecycle transitions and event publication. [Ticket %s]', n)
                    WHEN 8 THEN format('Create endpoint for task dependency graph retrieval and cycle detection response. [Ticket %s]', n)
                    ELSE format('Add database migration for task tags with many-to-many relation and cleanup script. [Ticket %s]', n)
                END
            ELSE
                CASE (n - 1) % 10
                    WHEN 0 THEN format('Provision PostgreSQL read replica in Terraform and configure failover monitoring alerts. [Ticket %s]', n)
                    WHEN 1 THEN format('Create Helm values for environment-specific task service autoscaling policies. [Ticket %s]', n)
                    WHEN 2 THEN format('Define CI pipeline stage for static security scans and policy gate enforcement. [Ticket %s]', n)
                    WHEN 3 THEN format('Implement Kubernetes network policies isolating API, DB, and worker namespaces. [Ticket %s]', n)
                    WHEN 4 THEN format('Automate secrets rotation workflow for SMTP and database credentials via Vault. [Ticket %s]', n)
                    WHEN 5 THEN format('Add Terraform module for log retention buckets with lifecycle rules per environment. [Ticket %s]', n)
                    WHEN 6 THEN format('Tune Prometheus alerts for task processing latency and scheduler misfire rates. [Ticket %s]', n)
                    WHEN 7 THEN format('Set up blue/green deployment strategy for API service with automated health checks. [Ticket %s]', n)
                    WHEN 8 THEN format('Create disaster recovery runbook and validate backup restore timing objectives. [Ticket %s]', n)
                    ELSE format('Introduce infrastructure cost dashboard tags for task platform compute resources. [Ticket %s]', n)
                END
        END AS description
    FROM seed
)
INSERT INTO task (
    name,
    description,
    createDate,
    deadline,
    status,
    statusUpdate,
    assignee,
    reporter
)
SELECT
    left(format('%s Task %s', stream, n), 50) AS name,
    left(description, 250) AS description,
    create_date AS createDate,
    create_date + (((n - 1) % 21) + 3) * INTERVAL '1 day' AS deadline,
    status,
    create_date + (((n - 1) % 5) + 1) * INTERVAL '1 day' AS statusUpdate,
    assignee,
    reporter
FROM descriptions
ORDER BY n;


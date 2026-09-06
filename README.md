# Poultry Prophet — Backend (Spring Boot)

Java/Spring Boot implementation of the backend described in the *Poultry Prophet* Software
Design Description (SDD). It replaces the SDD's Node.js/Express stack with an equivalent
Spring stack and implements all three modules.

| SDD (Node.js)                | This project (Spring Boot)                    |
|------------------------------|-----------------------------------------------|
| Express.js REST API          | Spring Web (`@RestController`)                |
| Prisma ORM + PostgreSQL      | Spring Data JPA / Hibernate + PostgreSQL      |
| JWT + bcrypt + role access   | Spring Security, jjwt, BCrypt, method security|
| Zod validation               | Jakarta Bean Validation (`@Valid`)            |
| Socket.IO push               | STOMP over WebSocket (`SimpMessagingTemplate`)|
| BullMQ worker                | `@Async` + `@TransactionalEventListener`      |
| PDFKit / csv-stringify       | OpenPDF + hand-rolled CSV                      |
| node-cron                    | (not required by current scope)               |

## Requirements

- **JDK 21+** (developed/tested building on JDK 25; bytecode target is 21).
- A **Supabase** (PostgreSQL) database.
- Maven — use the bundled wrapper (`./mvnw`), no system Maven needed.

## Configure the database (Supabase)

In Supabase: **Project Settings → Database → Connection string**. Use the **Session pooler**
(port `5432`) URI for JPA/Hibernate. Provide the connection via environment variables or a
local, git-ignored `.env` file in the repository root. Spring Boot loads `.env` automatically
when the application starts.

```bash
# PowerShell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME="postgres.<project-ref>"
$env:SPRING_DATASOURCE_PASSWORD="<your-db-password>"
$env:JWT_SECRET="<a base64 string of at least 32 bytes>"
```

`spring.jpa.hibernate.ddl-auto=update` creates/updates tables automatically on first run.
On startup a `DataSeeder` inserts the game fowl lifecycle stages (`brooding`, `ranging`,
`pre-conditioning`, `maintenance`, `conditioning`) and default alert thresholds
(BHI 60–100, BSI 0–40, WFR 1.5–2.5). MVP scope covers brooding + ranging through the
month-5 selection decision; the later stages are seeded for lifecycle correctness only.

## Build & run

```bash
./mvnw clean package          # build (Windows: .\mvnw.cmd clean package)
./mvnw spring-boot:run        # run on http://localhost:8080
# or:
java -jar target/poultry-prophet-backend-0.1.0.jar
```

> **JDK 25 note:** Hibernate's ByteBuddy may not officially recognise very new JDKs. The
> app sets `net.bytebuddy.experimental=true` at startup to allow it. If you hit a bytecode
> error, run on JDK 21 instead.

## Auth quick start

```bash
# 1. Register a manager (open endpoint for bootstrapping)
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"email":"manager@farm.test","password":"password123","fullName":"Farm Manager","role":"MANAGER","farmId":1}'

# 2. Register a handler on the same farm
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"email":"handler@farm.test","password":"password123","fullName":"Handler One","role":"HANDLER","farmId":1}'

# 3. Login -> copy the "token"
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"manager@farm.test","password":"password123"}'
```

Send `Authorization: Bearer <token>` on all other requests.

## API overview

### Module 1 — Data Input & Collection
- `GET  /api/lifecycle-stages` — stage dropdown.
- `GET  /api/handlers` — handlers on your farm (for assignment).
- `POST /api/batches` *(MANAGER)* — register a batch (1.3); accepts `bloodline` and `source` as
  descriptive Stage-0 metadata (bloodline is collected but **not** used in scoring, Blueprint 5.3).
- `GET  /api/batches`, `GET /api/batches/{id}`.
- `PATCH /api/batches/{id}/stage` *(MANAGER)* — advance the batch through the lifecycle (e.g. brooding → ranging).
- `POST /api/batches/{id}/records` — record a daily **brooding** entry (1.1); idempotent per (batch, date).
- `GET  /api/batches/{id}/records?limit=14` — recent submissions.
- `POST /api/sync/batch` — sync buffered offline entries with conflict resolution (1.2).
- `POST /api/batches/{id}/birds` — band an individual bird (Blueprint 5.4); unique band number per batch.
- `GET  /api/batches/{id}/birds` — list banded birds.
- `POST /api/batches/{id}/birds/{birdId}/ranging` — weekly per-bird ranging milestone
  (weight, health event severity, temperament, C/B+/A/A++ rating); idempotent per (bird, date).
- `GET  /api/batches/{id}/birds/{birdId}/ranging` — that bird's ranging history.

### Module 2 — Data Processing & Analytics
- Indicators (BHI/BSI/WFR/readiness) are computed asynchronously after each record write (2.1/2.2).
- `GET  /api/batches/{id}/indicators/latest`, `GET /api/batches/{id}/indicators?limit=14`.
- `GET  /api/thresholds`, `PUT /api/thresholds/{id}` *(MANAGER)* — editable thresholds (2.4).
- Alerts are generated automatically when an indicator breaches its threshold (2.3).
- `GET  /api/batches/{id}/alerts?activeOnly=true`.
- `POST /api/alerts/{id}/acknowledge` *(MANAGER)*.
- A new alert generates one operational intervention recommendation for the affected batch.
- `GET  /api/interventions?status=PENDING` — farm-scoped intervention queue. Managers see all
  interventions; handlers see interventions for their assigned batches.
- `GET  /api/batches/{id}/interventions` — interventions for one batch.
- `GET  /api/interventions/{id}/history` — immutable intervention action history.
- `POST /api/interventions/{id}/claim` *(HANDLER)* — claim an unassigned intervention.
- `POST /api/interventions/{id}/start` *(HANDLER)* — begin the checklist.
- `POST /api/interventions/{id}/complete` *(HANDLER)* — complete it with optional outcome notes.
- `POST /api/interventions/{id}/escalate` *(HANDLER)* — escalate with a required note.
- `PUT /api/interventions/{id}/assignment` *(MANAGER)* — assign to a handler assigned to the batch.
- `POST /api/interventions/{id}/dismiss` *(MANAGER)* — close an intervention with a required reason.

### Conditioning Readiness Scoring & Month-5 Selection (Blueprint section 6 — the keystone)
- `GET  /api/batches/{id}/selection` *(MANAGER)* — recomputes scores and returns the **ranked
  selection view**: birds ordered by Conditioning Readiness Score (CRS) desc, each row exposing
  its four sub-scores (Brooding Health Index, Growth, Health History, Behavioural) for
  transparency, plus a suggested advancement cut-line and the system recommendation.
- `POST /api/batches/{id}/selection/birds/{birdId}` *(MANAGER)* — record the breeder's
  confirm/override decision (`advance` true/false). An override (decision ≠ recommendation)
  **requires a `reason`**, recorded as research data.

The engine (`scoring/ScoringService`) is deterministic, transparent and adjustable:
`CRS = 0.30·BHI + 0.30·Growth + 0.20·HealthHistory + 0.20·Behavioural`. All weights, the
mortality band, growth penalty, health deductions, expected-weight curve and cut-line are
**provisional, configuration-driven** starting values under `poultry.scoring.*` in
`application.yml` — not established facts.

### Module 3 — Data Output & Visualization
- `GET  /api/batches/{id}/overview` — dashboard composite payload (3.1), including active
  interventions alongside indicators, records, and alerts.
- `GET  /api/batches/{id}/reports?start=YYYY-MM-DD&end=YYYY-MM-DD` *(MANAGER)* — build report (3.2).
- `POST /api/reports/{id}/export?format=pdf|csv` *(MANAGER)* — download PDF/CSV.
- WebSocket: connect to `/ws` (SockJS), send `Authorization: Bearer <token>` on CONNECT,
  subscribe to `/topic/farms/{farmId}/alerts`, `/topic/farms/{farmId}/indicators`, and
  `/topic/farms/{farmId}/interventions` (3.3).

Interventions are deterministic operational checklists generated from alert type and severity.
They are not veterinary diagnoses or treatment instructions. Critical recommendations ask the
handler to notify the manager before any high-risk action. Handler claims, progress updates,
completion notes, escalations, manager assignments, and dismissals are recorded in intervention
history.

## Notes on provisional design (per SDD preface)

The BHI/BSI/WFR formulas and severity bands are **provisional** and live in
`AnalyticsService` (weights in `application.yml` under `poultry.analytics`) and
`SeverityClassifier`. Thresholds are DB-backed and editable at runtime. Likewise the
Conditioning Readiness scoring formulas live in `ScoringService` with all weights/thresholds
under `poultry.scoring` (`ScoringProperties`). These are the sections most likely to change
once the SRS is approved. Per the blueprint's honesty rule, literature-backed thresholds are
evidence-based while weights and point deductions are documented, adjustable design decisions —
never presented as established facts.

## Intervention implementation guide

The intervention system turns an analytics alert into a trackable operational checklist. It is
intentionally deterministic and operational: it does not diagnose disease or prescribe
veterinary treatment.

### End-to-end flow

```text
DailyRecord write
    ↓
IndicatorJobWorker recomputes BHI / BSI / WFR
    ↓
AlertService evaluates the indicator against the effective threshold
    ↓
Alert is saved and published to /topic/farms/{farmId}/alerts
    ↓
InterventionService.createForAlert(alert)
    ↓
Intervention is saved once and published to /topic/farms/{farmId}/interventions
    ↓
Manager or handler works the checklist and every action is audited
```

The indicator worker runs asynchronously after the record transaction commits. An alert is
created only when the value breaches its threshold and there is no existing unacknowledged alert
of the same type for the batch. `InterventionService.createForAlert` is idempotent by `alert_id`,
so retrying alert processing does not create duplicate interventions.

### Main implementation files

| Responsibility | File |
|---|---|
| Intervention entity and database mapping | `src/main/java/com/poultryprophet/intervention/Intervention.java` |
| Status values | `src/main/java/com/poultryprophet/intervention/InterventionStatus.java` |
| Alert-to-checklist rules | `src/main/java/com/poultryprophet/intervention/InterventionRecommendationCatalog.java` |
| Business rules and authorization checks | `src/main/java/com/poultryprophet/intervention/InterventionService.java` |
| REST endpoints | `src/main/java/com/poultryprophet/intervention/InterventionController.java` |
| Immutable action audit trail | `src/main/java/com/poultryprophet/intervention/InterventionHistory.java` |
| API response/request contracts | `src/main/java/com/poultryprophet/intervention/dto/` |
| Real-time intervention updates | `src/main/java/com/poultryprophet/realtime/RealtimeNotificationService.java` |

### Checklist catalog

`InterventionRecommendationCatalog` selects a checklist from the alert indicator type:

| Alert type | Default checklist | Critical-alert behavior |
|---|---|---|
| `BHI` | Check brooding conditions, ventilation, litter, feed, and water | Tells the handler to notify the manager before high-risk action |
| `BSI` | Inspect bird behavior and environment | Requires manager review |
| `WFR` | Inspect drinkers and feeders, then verify readings | Requires manager review |
| Any other type | Review the affected batch and latest records | Uses the same critical escalation prefix |

To add a new checklist, add a case to `recommend(Alert)` and keep the recommendation focused on
safe observations and operational checks. Do not add diagnoses, medication, dosage, or treatment
instructions to this catalog.

### Intervention lifecycle

The supported statuses are:

```text
PENDING → ACKNOWLEDGED → IN_PROGRESS → COMPLETED
    │           │              │
    └───────────┴──────────────┴────────→ ESCALATED
                                      │
                                      └─ manager assignment → PENDING

PENDING / ACKNOWLEDGED / IN_PROGRESS / ESCALATED → DISMISSED
```

Operational rules:

- A handler can claim an unassigned `PENDING` intervention. Claiming assigns it to the handler
  and changes the status to `ACKNOWLEDGED`.
- A handler can start an intervention assigned to them from `PENDING` or `ACKNOWLEDGED`.
- A handler can complete an assigned intervention from `ACKNOWLEDGED` or `IN_PROGRESS`.
- A handler can escalate an assigned intervention from `ACKNOWLEDGED` or `IN_PROGRESS`; an
  escalation note is required.
- A manager can assign or reassign an intervention unless it is terminal or `IN_PROGRESS`.
- A manager can dismiss any non-terminal intervention; a dismissal reason is required.
- `COMPLETED` and `DISMISSED` are terminal statuses and cannot be assigned or acted on again.
- Every successful create, claim, start, complete, escalate, assignment, and dismissal writes an
  `InterventionHistory` row.

### REST API guide

All requests require `Authorization: Bearer <token>`. IDs below are examples.

List the manager's farm queue or the handler's assigned-batch queue:

```bash
curl "http://localhost:8080/api/interventions?status=PENDING&limit=100" \
  -H "Authorization: Bearer <token>"
```

List interventions for one batch, inspect one intervention, or read its audit history:

```bash
curl "http://localhost:8080/api/batches/12/interventions" \
  -H "Authorization: Bearer <token>"

curl "http://localhost:8080/api/interventions/44" \
  -H "Authorization: Bearer <token>"

curl "http://localhost:8080/api/interventions/44/history" \
  -H "Authorization: Bearer <token>"
```

Handler workflow:

```bash
curl -X POST "http://localhost:8080/api/interventions/44/claim" \
  -H "Authorization: Bearer <handler-token>"

curl -X POST "http://localhost:8080/api/interventions/44/start" \
  -H "Authorization: Bearer <handler-token>"

curl -X POST "http://localhost:8080/api/interventions/44/complete" \
  -H "Authorization: Bearer <handler-token>" \
  -H "Content-Type: application/json" \
  -d '{"note":"Checked ventilation and water access; readings returned to normal."}'
```

Escalate when manager review is needed:

```bash
curl -X POST "http://localhost:8080/api/interventions/44/escalate" \
  -H "Authorization: Bearer <handler-token>" \
  -H "Content-Type: application/json" \
  -d '{"note":"Mortality continues after environmental checks; manager review required."}'
```

Manager workflow:

```bash
curl -X PUT "http://localhost:8080/api/interventions/44/assignment" \
  -H "Authorization: Bearer <manager-token>" \
  -H "Content-Type: application/json" \
  -d '{"handlerUserId":7}'

curl -X POST "http://localhost:8080/api/interventions/44/dismiss" \
  -H "Authorization: Bearer <manager-token>" \
  -H "Content-Type: application/json" \
  -d '{"note":"Reviewed and confirmed the alert was a transient reading."}'
```

Available endpoints:

| Method | Endpoint | Role |
|---|---|---|
| `GET` | `/api/interventions?status=&limit=` | Manager: farm-wide; handler: assigned batches |
| `GET` | `/api/batches/{batchId}/interventions?status=&limit=` | Manager or assigned handler |
| `GET` | `/api/interventions/{id}` | Manager or handler in the same batch |
| `GET` | `/api/interventions/{id}/history` | Manager or handler in the same batch |
| `POST` | `/api/interventions/{id}/claim` | Handler |
| `POST` | `/api/interventions/{id}/start` | Handler assigned to intervention |
| `POST` | `/api/interventions/{id}/complete` | Handler assigned to intervention |
| `POST` | `/api/interventions/{id}/escalate` | Handler assigned to intervention |
| `PUT` | `/api/interventions/{id}/assignment` | Manager |
| `POST` | `/api/interventions/{id}/dismiss` | Manager |

### Frontend integration

The frontend should treat the intervention response as the source of truth and invalidate the
intervention queue, batch intervention list, and batch overview after each mutation. The most
important fields are `status`, `managerReviewRequired`, `assignedHandlerId`, `instructions`, and
`outcomeNote`.

For live updates, connect to `/ws`, authenticate on the STOMP `CONNECT` frame, and subscribe to:

```text
/topic/farms/{farmId}/interventions
```

Each message uses the `InterventionEvent` shape:

```json
{
  "interventionId": 44,
  "alertId": 91,
  "batchId": 12,
  "indicatorType": "BHI",
  "severity": "CRITICAL",
  "title": "Check batch brooding conditions",
  "status": "PENDING",
  "assignedHandlerId": null,
  "occurredAt": "2026-09-06T14:00:00Z"
}
```

On receipt, refetch the affected intervention or invalidate the relevant React Query keys. The
WebSocket event is a notification, not a replacement for the authenticated REST response.

### Testing and extension checklist

When changing the intervention system, verify at least:

1. Each supported indicator type produces the expected checklist.
2. Reprocessing the same alert returns the existing intervention instead of creating a duplicate.
3. Managers can see farm interventions while handlers see only assigned-batch interventions.
4. A handler cannot claim an intervention assigned to another handler or act on an unclaimed one.
5. Required escalation and dismissal notes reject blank or whitespace-only values.
6. Invalid status transitions return a conflict response and do not write audit history.
7. Terminal interventions cannot be reassigned or acted on again.
8. Every successful state mutation creates the expected immutable history entry and real-time event.

The catalog behavior is covered by
`src/test/java/com/poultryprophet/intervention/InterventionRecommendationCatalogTest.java`.
Add service-level tests for any new transition, authorization rule, or checklist branch.

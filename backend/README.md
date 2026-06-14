# Backend Template for SE PR Group Phase

## How to run it

### Start the backed
`mvn spring-boot:run`

### Start the backed with test data
If the database is not clean, the test data won't be inserted

`mvn spring-boot:run -Dspring-boot.run.profiles=generateData`

### FR-15 fair chore demo data

The `generateData` profile also creates a deterministic household for manual FR-15 checks:

```text
Household: Fair Chore Demo WG
Login:     maintenant.fr15@example.com
Password:  password
```

Seeded roommates use the same password:

```text
roommate.heavy@example.com   Heavy Worker
roommate.medium@example.com  Medium Worker
roommate.light@example.com   Light Worker
```

Expected default 30-day scores:

```text
Anna Fair      2 completed + 1 open-not-overdue = 2.75, Bucket A
Heavy Worker   5 completed + 1 open-not-overdue = 5.75, Bucket A
Medium Worker  3 completed + 1 overdue          = 5.0, Bucket C
Light Worker   1 completed                      = 1.0, Bucket A
```

The bucket algorithm first considers Bucket A (`NO_OVERDUE_AVAILABLE`) and then selects the exact minimum score in that bucket,
so Light Worker is the only fair candidate and should be recommended in task creation.
The seed data also includes an old Light Worker completion outside the 30-day window and a second noise household whose tasks
must not affect the Fair Chore Demo WG calculation.

Manual check:

```bash
cd backend
mvn -Dmaven.gitcommitid.skip=true spring-boot:run -Dspring-boot.run.profiles=generateData

cd ../frontend
npm start -- --host 127.0.0.1 --port 4200
```

Open `http://127.0.0.1:4200/#/tasks/create`, log in as `maintenant.fr15@example.com` / `password`, verify the fair chore
suggestion recommends Light Worker, click "Use suggestion", then create the task and check that it is assigned to Light Worker.

## Development-only secrets

The default `application.yml` values are local development/demo fallbacks only. They are not production credentials and must not be used for a real deployment.

For any deployed environment, provide secrets through environment variables or deployment configuration, for example:

```bash
export JWT_SECRET="replace-with-a-long-random-secret"
export DB_USERNAME="wgms_user"
export DB_PASSWORD="replace-with-a-real-database-password"
```

The test profile uses `backend/src/test/resources/application-test.yml` with artificial test-only values so automated tests remain reproducible.

### Production profile secrets

The `prod` profile intentionally does not define fallback values for security-sensitive settings. A production-like run must provide at least:

```bash
export JWT_SECRET="replace-with-a-long-random-secret"
export DB_USERNAME="wgms_user"
export DB_PASSWORD="replace-with-a-real-database-password"
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The committed fallback values in the default profile are for local university-demo development only. They are not real deployed credentials.

## FR-15 Fair chore balancing

Task creation can request a fair chore suggestion from `GET /api/v1/tasks/fair-balancing/recommendation`.
The backend derives the current household from the authenticated user's active roommate record; clients do not supply a trusted
household id.

The default history window is 30 days. For every active, non-anonymized roommate in the household, the backend first assigns a fairness bucket:

- `NO_OVERDUE_AVAILABLE`: no overdue tasks and fewer than 3 open non-overdue tasks.
- `NO_OVERDUE_OVERLOADED`: no overdue tasks and 3 or more open non-overdue tasks.
- `HAS_OVERDUE`: at least one overdue task.

The recommendation prefers the first non-empty bucket in that order. Inside the selected bucket, the score is:

```text
completedTasksInHistoryWindow * 1.0
+ openTasksNotOverdue * 0.75
+ overdueTasks * 2.0
```

Lower score means less recent chore burden. Overdue tasks are counted as overdue, not also as open. Because tasks currently store
status and `updatedAt` but no dedicated `completedAt`, current `DONE` tasks updated inside the window are used as the completion
signal; undone tasks no longer have status `DONE` and are scored by their current state.

Only roommates in the selected bucket with the exact minimum score form the candidate pool. If there is one candidate, that roommate
is recommended. If there is an exact tie, seeded randomness based on stable context chooses between the tied roommates, so repeated
requests with the same task title/due date return the same recommendation while different contexts can rotate only within the true
minimum-score tie group.

The Angular task creation form shows the suggestion and offers a "Use suggestion" action. Users can still manually choose any
household assignee afterwards.

Cleaning schedules keep their fixed assignee by default. When `fairBalancingEnabled` is set to `true`, generated recurring tasks
use the same fair balancing service with a seed based on household id, schedule id, and next due date; the configured assignee
remains the fallback.

# Backend Template for SE PR Group Phase

## How to run it

### Start the backed
`mvn spring-boot:run`

### Start the backed with test data
If the database is not clean, the test data won't be inserted

`mvn spring-boot:run -Dspring-boot.run.profiles=generateData`

## Development-only secrets

The default `application.yml` values are local development/demo fallbacks only. They are not production credentials and must not be used for a real deployment.

For any deployed environment, provide secrets through environment variables or deployment configuration, for example:

```bash
export JWT_SECRET="replace-with-a-long-random-secret"
export DB_USERNAME="wgms_user"
export DB_PASSWORD="replace-with-a-real-database-password"
```

Optional email delivery via [Resend](https://resend.com):

The team Resend API key is documented in the **group chat** (not in Git).

Before starting the backend, export:

```bash
export WGMS_MAIL_PROVIDER=resend
export RESEND_API_KEY="<key from team wiki>"
export WGMS_MAIL_FROM="WGMS <noreply@shr8.sbs>"
mvn spring-boot:run
```

E2E and CI runs must keep verification disabled (`WGMS_MAIL_VERIFICATION_ENABLED=false`, the default). Real Resend delivery uses the verified `shr8.sbs` sender domain above; Cypress tests still register with fake `@test.com` / `@example.com` addresses.

Without Resend configured, the backend uses the mock mail provider and still persists in-app notifications.

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

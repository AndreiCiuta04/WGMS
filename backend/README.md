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

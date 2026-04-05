# Railway deployment configuration

`railway.toml` is the code-owned copy of the live Railway build/deploy settings for the `server` service in the `fantazzk` project.

The current live setup keeps only the `production` environment on Railway. If a non-production environment is reintroduced later, mirror its explicit build/deploy settings under `environments.<name>` in `railway.toml`.

## What lives in code

- `builder`
- environment-specific deploy settings such as `healthcheckPath`, `drainingSeconds`, replica placement, and CPU/memory limits

## What stays in Railway

- secrets and runtime variables such as `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `SENTRY_DSN`, `SPRING_PROFILES_ACTIVE`, and Swagger credentials
- source wiring such as repository/branch connection
- domains and any future service settings that are not present in `railway.toml`

## How to refresh from live Railway settings

1. Link this repository to the Railway project/service if needed.
   Run: `railway link -p <project-id> -s <service-id> -e production`
2. Inspect the live production settings.
   Run: `railway environment config -e production --json`
3. Update `railway.toml` to match the current explicit `build` and `deploy` settings.

## Variables

To inspect production variables without opening the dashboard:

- `railway variable list -e production --json`

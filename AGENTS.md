# AGENTS.md

## Project overview

智学伴 (Study Companion) — AI-powered study tool for civil service exam prep. Three-tier monorepo:

- `backend/` — Spring Boot 3.2.5, Java 17, MyBatis-Plus 3.5.5
- `frontend/` — Vue3 + Vite + Element Plus + Pinia
- `miniapp/` — Uniapp (empty, not yet started)

## Quick commands

### Backend (from repo root)

```bash
mvn compile -f backend/pom.xml          # compile check
mvn test -f backend/pom.xml             # run tests
mvn spring-boot:run -f backend/pom.xml  # run server (port 8080)
```

### Frontend (from `frontend/`)

```bash
npm install --legacy-peer-deps   # MUST use --legacy-peer-deps (peer dep conflicts)
npm run dev                      # dev server on port 3000
npm test                         # vitest (single run)
npm run build                    # production build
```

## Dev environment

DevContainer provides MySQL 8.0 and Redis 7 via docker-compose. Forwarded ports: 3000, 8080, 3306, 6379.

- MySQL: `root:root@localhost:3306`, database `study_companion`
- Redis: `localhost:6379`, no password
- DB init script: `backend/sql/init.sql` (auto-loaded by docker-compose and `start.sh`)

## Backend conventions

- **Package**: `com.studycompanion` — subpackages: `entity`, `mapper`, `service/impl`, `controller`, `dto`, `vo`, `config`, `common`
- **Entities** extend `BaseEntity` (provides `id`, `createTime`, `updateTime` with auto-fill)
- **Response wrapper**: `Result<T>` with `code`/`message`/`data` — use `Result.success(data)` / `Result.error(ErrorCode.XXX)`
- **Error codes**: `ErrorCode` enum — module ranges: 1xxx user, 2xxx subject, 3xxx record, 4xxx checkin, 5xxx diary, 6xxx goal, 7xxx miss, 8xxx AI
- **API prefix**: `/api/v1/` — JWT auth via `JwtInterceptor`, excludes `/api/v1/auth/**`
- **Validation**: Jakarta Validation on DTOs, `@Valid` on controller params
- **Maps**: MapStruct for DTO↔Entity conversion (annotation processor configured in pom.xml)
- **Soft delete**: MyBatis-Plus logical delete on `deleted` field (0/1)
- **Logical delete value**: 1=deleted, 0=not deleted

## Frontend conventions

- **Path alias**: `@` → `src/` (Vite config)
- **Auto-imports**: Element Plus components and Vue/Vue Router/Pinia APIs auto-imported (unplugin-vue-components + unplugin-auto-import)
- **API layer**: `src/api/index.js` (axios instance, interceptors) + `src/api/modules.js` (all endpoint functions grouped by domain)
- **Stores**: Pinia composition API style (`defineStore` with setup function) in `src/stores/`
- **Routing**: lazy-loaded, auth guard checks `localStorage.token`, redirects unauthenticated to `/login`
- **Testing**: Vitest with `happy-dom` environment, tests in `src/__tests__/`

## CI (`.github/workflows/ci.yml`)

Runs on push/PR to `main`:
1. Backend: `mvn compile` → `mvn test`
2. Frontend: `npm ci --legacy-peer-deps` → `npm test` → `npm run build`

## Gotchas

- Frontend npm install **requires** `--legacy-peer-deps` — bare `npm install` will fail
- Backend `application.yml` uses `spring.profiles.active: dev` — no separate profile configs exist, dev is the only one
- AI API keys are placeholder (`your-api-key`) in `application.yml` — AI features won't work without real keys
- `miniapp/` is empty — do not reference or generate code for it
- Backend test classes mirror main package structure under `src/test/java/com/studycompanion/`

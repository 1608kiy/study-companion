# AGENTS.md

## Project overview

智学伴 (Study Companion) — AI-powered study tool for civil service exam prep. Three-tier monorepo:

- `backend/` — Spring Boot 3.2.5, Java 17, MyBatis-Plus 3.5.5
- `frontend/` — Vue3 + Vite + Element Plus + Pinia
- `miniapp/` — Uniapp 3.x (Vue3) 通用小程序，支持微信/支付宝/百度/抖音/QQ

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

### Miniapp (from `miniapp/`)

```bash
npm install                      # install dependencies
npm run dev:mp-weixin            # dev for WeChat
npm run dev:mp-alipay            # dev for Alipay
npm run dev:h5                   # dev for H5 preview
npm run build:mp-weixin          # build for WeChat
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
- **AI integration**: MiMo via `AiClient.java` (generic OpenAI-compatible HTTP client)
- **API endpoints**: Auth, User, Subject, StudyRecord, Goal, Diary, CheckIn, AI

## Frontend conventions

- **Path alias**: `@` → `src/` (Vite config)
- **Auto-imports**: Element Plus components and Vue/Vue Router/Pinia APIs auto-imported (unplugin-vue-components + unplugin-auto-import)
- **API layer**: `src/api/index.js` (axios instance, interceptors) + `src/api/modules.js` (all endpoint functions grouped by domain)
- **Stores**: Pinia composition API style (`defineStore` with setup function) in `src/stores/`
- **Routing**: lazy-loaded, auth guard checks `localStorage.token`, redirects unauthenticated to `/login`
- **Testing**: Vitest with `happy-dom` environment, tests in `src/__tests__/`
- **Styling**: Flat Remix style — no gradients, no shadows, no decorative elements, solid colors, border-based hierarchy
- **CSS variables**: All colors defined as CSS variables in `src/assets/main.css`, zero hardcoded colors in Vue files
- **Responsive**: All views support mobile (768px breakpoint), sidebar becomes drawer on mobile
- **Security**: DOMPurify for markdown rendering, API key via environment variable (never committed)

## Miniapp conventions

- **Framework**: Uniapp 3.x with Vue3 Composition API (`<script setup>`)
- **State management**: Pinia (`store/user.js`, `store/study.js`)
- **API layer**: `api/index.js` (uni.request wrapper) + `api/modules.js` (all endpoints)
- **Config**: `config/index.js` — environment-based BASE_URL (H5 proxy vs production)
- **Pages**: 9 pages (5 tabs + 4 sub-pages) in `pages/` directory
- **Components**: Custom `tab-bar.vue` + `main-layout.vue` (replaces native tabBar)
- **Styling**: Shared styles in `uni.scss`, all pages use global classes
- **Error handling**: `utils/error-handler.js` — Vue errors, unhandled rejections, page not found
- **Share**: `utils/share.js` — WeChat share to friends and timeline
- **Version**: `utils/version.js` — auto update check, version display
- **Markdown**: `utils/markdown.js` — simple markdown renderer using `rich-text` component
- **Platform**: Supports WeChat, Alipay, Baidu, TikTok, QQ, H5

## API endpoints

| Module | Endpoints | Description |
|--------|-----------|-------------|
| Auth | POST `/api/v1/auth/login`, `/api/v1/auth/register` | JWT authentication |
| User | GET/PUT `/api/v1/user/profile`, DELETE `/api/v1/user/delete` | User management |
| Subject | CRUD `/api/v1/subjects`, GET `/api/v1/subjects/preset` | Study subjects |
| StudyRecord | CRUD `/api/v1/study-records`, timer endpoints, GET `/api/v1/study-records/stats` | Study tracking |
| Goal | CRUD `/api/v1/goals`, daily/weekly/monthly stats, calendar stats | Goal management |
| Diary | CRUD `/api/v1/diaries`, generate, regenerate, images | AI diary |
| CheckIn | POST `/api/v1/check-in`, history, miss, replenish | Attendance |
| AI | POST `/api/v1/ai/chat`, focus-judge, weekly/monthly report | AI features |

## CI (`.github/workflows/ci.yml`)

Runs on push/PR to `main`:
1. Backend: `mvn compile` → `mvn test`
2. Frontend: `npm ci --legacy-peer-deps` → `npm test` → `npm run build`

## Gotchas

- Frontend npm install **requires** `--legacy-peer-deps` — bare `npm install` will fail
- Backend `application.yml` uses `spring.profiles.active: dev` — no separate profile configs exist, dev is the only one
- AI API keys are placeholder (`your-api-key`) in `application.yml` — set `AI_API_KEY` environment variable
- `miniapp/` is now complete with 9 pages — uses custom tab-bar component, not native tabBar
- Backend test classes mirror main package structure under `src/test/java/com/studycompanion/`
- MySQL uses `mysql_native_password` authentication (changed from `auth_socket` for JDBC compatibility)
- Server memory is tight (1.6GB) — stop MySQL/Redis when not in use

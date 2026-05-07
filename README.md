# Autonomous LinkedIn Content Architect

An AI-powered full-stack application that helps you generate, refine, schedule, and publish LinkedIn posts.

It combines:
- **Angular Frontend** for interactive post generation and scheduling
- **Spring Boot Backend** for AI orchestration, persistence, scheduling, and LinkedIn integration
- **Gemini (Spring AI)** for content generation and post enhancement
- **MySQL** for post history and scheduling state

## How the project works

1. User enters topic, audience, tone, goal, and length in the frontend.
2. Frontend calls backend streaming endpoint to generate post content in real time.
3. When streaming is complete, frontend saves the post to the database.
4. User can copy, review history, mark as published, or schedule for later.
5. Scheduler service checks due scheduled posts and publishes them to LinkedIn.
6. Daily autonomous agent can generate and publish content on a schedule.

## Project structure

```text
Autonomous-LinkedIn-Content-Architect/
├── Backend/
│   ├── src/main/java/com/example/project/
│   │   ├── Controller/          # REST endpoints (post + LinkedIn OAuth helpers)
│   │   ├── services/            # AI generation, post processing, scheduling, publishing
│   │   ├── Entity/              # JPA entities
│   │   ├── Repository/          # Spring Data repositories
│   │   ├── config/              # CORS and security configuration
│   │   └── ProjectApplication.java
│   ├── src/main/resources/application.properties
│   └── pom.xml
├── frontend/
│   ├── src/app/
│   │   ├── app.ts               # Main UI logic (standalone Angular root component)
│   │   └── services/post.service.ts
│   ├── package.json
│   └── nginx.conf
├── docker-compose.yml
└── .github/workflows/deploy.yml
```

## API endpoints

### Post endpoints (`/api/posts`)

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/posts/generate/stream` | Streams AI-generated LinkedIn post content (SSE) |
| `POST` | `/api/posts/save` | Processes and saves generated content as a draft |
| `GET` | `/api/posts/history` | Returns saved post history (latest first) |
| `PATCH` | `/api/posts/{id}/publish` | Marks a post as published |
| `POST` | `/api/posts/{id}/schedule` | Schedules a draft post at a specific date-time |
| `GET` | `/api/posts/scheduled` | Returns currently scheduled posts |
| `DELETE` | `/api/posts/{id}/schedule` | Cancels a scheduled post |

### LinkedIn OAuth helper endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/callback` | Exchanges LinkedIn OAuth authorization code for access token |
| `GET` | `/me?token=...` | Retrieves LinkedIn user identity (person URN) for posting |

## Use cases

- **Content ideation and drafting**: Generate high-quality LinkedIn posts from a topic and audience.
- **Real-time writing experience**: Watch post content stream into the UI as it is generated.
- **Post polishing**: Automatically improve formatting, spacing, and emoji placement before saving.
- **Content calendar workflow**: Schedule posts and let backend publish at the right time.
- **Publishing traceability**: Keep full history of drafts, scheduled posts, posted items, and failures.
- **Autonomous posting**: Run a scheduled agent that decides a topic, generates, polishes, and publishes.

## Local setup (quick start)

### Prerequisites
- Java 21
- Maven (or use `./mvnw`)
- Node.js 20+
- MySQL 8+
- Gemini API key
- LinkedIn app credentials

### Backend
1. Configure `Backend/src/main/resources/application.properties` placeholders:
   - `DB_PW`
   - `GEMINI_KEY`
   - `Client_Id`
   - `Client_Secret`
   - `Access_token`
   - `URN`
2. Run backend:
   ```bash
   cd Backend
   ./mvnw spring-boot:run
   ```

### Frontend
```bash
cd frontend
npm install
npm start
```

Frontend runs on `http://localhost:4200` and backend runs on `http://localhost:8081`.

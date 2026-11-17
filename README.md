# Personal Notes API

A Kotlin and Spring Boot REST API for personal notes. Users register, sign in, and create, list, edit, or delete their own notes. The API stores data in MongoDB, hashes passwords with BCrypt, and uses JWT access and refresh tokens.

**Stack:** Kotlin 1.9.25, Spring Boot 3.4.3, Spring Security, MongoDB, and JJWT.

## Run locally

You need **JDK 17** and a running MongoDB instance, either locally or on MongoDB Atlas. The project includes the **Gradle 8.13 wrapper**.

### Environment configuration

From the repository root, copy the example configuration:

```bash
cp .env.example atlas-credentials.env
```

Edit the copied file:

```dotenv
MONGODB_URI=mongodb://localhost:27017
SPRING_DATA_MONGODB_DATABASE=personalnotesapi
JWT_SECRET_BASE64=
```

For Atlas, replace `MONGODB_URI` with your cluster's connection string, including the database user's credentials, and add your public IP to the Atlas IP access list.

Generate a JWT secret locally:

```bash
openssl rand -base64 32
```

Paste the complete result into `JWT_SECRET_BASE64`. Keep the same secret across restarts to keep existing tokens valid. The local `atlas-credentials.env` file is ignored by Git. Commit `.env.example` with example values only.

### Start with IntelliJ

Open **Run → Edit Configurations**, select the application configuration, and enable **Environment variables** under **Modify options** if needed. Use the environment file picker to select `atlas-credentials.env`, then apply the configuration and run the application.

### Start from the terminal

Spring Boot does not automatically load `.env` files. Export the three variables in your shell before starting the application:

```bash
export MONGODB_URI='mongodb://localhost:27017'
export SPRING_DATA_MONGODB_DATABASE='personalnotesapi'
export JWT_SECRET_BASE64='<your generated Base64 secret>'
./gradlew bootRun
```

Use your Atlas URI instead of the local URI when connecting to Atlas. The API runs at `http://localhost:8080`.

Run `./gradlew test` with the same environment variables to run the Spring context test.

## API endpoints

Send JSON bodies with `Content-Type: application/json`. All notes endpoints require an access token in the `Authorization: Bearer <accessToken>` header. Do not use the refresh token for notes endpoints.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/auth/register` | Register with `email` and `password` |
| POST | `/auth/login` | Sign in with the same fields and get an access and refresh token |
| POST | `/auth/refresh` | Send `refreshToken` to get a new token pair |
| GET | `/notes` | List your notes |
| POST | `/notes` | Create a note with a server-generated ID |
| PATCH | `/notes/{id}` | Update one or more provided fields |
| DELETE | `/notes/{id}` | Delete one of your notes |

Access tokens last 15 minutes. Refresh tokens last 30 days.

## Try it

Register and sign in:

```bash
curl -X POST http://localhost:8080/auth/register -H 'Content-Type: application/json' -d '{"email":"demo@example.com","password":"DemoPass123"}'
curl -X POST http://localhost:8080/auth/login -H 'Content-Type: application/json' -d '{"email":"demo@example.com","password":"DemoPass123"}'
```

Copy `accessToken` from the login response and use it to create a note. A note takes a non-blank `title`, a `content` string, and a numeric `color`.

```bash
curl -X POST http://localhost:8080/notes -H "Authorization: Bearer $ACCESS_TOKEN" -H 'Content-Type: application/json' -d '{"title":"First note","content":"Hello from Kotlin","color":16763904}'
```

Send the same authorization header with `GET /notes` to see the saved note. Copy its `id` from the response to update it.

Use `PATCH` to change only the fields in the request; omitted fields stay unchanged. For example, to change only the title:

```bash
curl -X PATCH 'http://localhost:8080/notes/<noteId>' -H "Authorization: Bearer $ACCESS_TOKEN" -H 'Content-Type: application/json' -d '{"title":"Renamed note"}'
```

An empty PATCH body returns `400 Bad Request`. Updates preserve the original creation date. An invalid note ID returns `400 Bad Request`. A missing note or a note owned by another user returns `404 Not Found`.

## Credits

Built following the [Spring Boot with Kotlin](https://www.youtube.com/watch?v=tXC9DQRWHUQ) course.

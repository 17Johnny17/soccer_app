# Soccer App

Hi, my name is Jonathan. I am building this soccer application to deepen my understanding of Java, Spring Boot, and software architecture. My goal is to turn the concepts I learn into working, tested code by adding features incrementally.

The project currently provides a REST endpoint for retrieving player profiles from API-FOOTBALL. It validates the provider's response and translates missing players and upstream failures into HTTP responses.

## Tech stack

- **Java 21**
- **Spring Boot 4.1.1**
- **Maven**, with the Maven Wrapper included
- **Spring Web MVC** for REST endpoints
- **Spring RestClient** for external HTTP requests
- **Spring Security** for default application security
- **Spring Boot Actuator** for application monitoring
- **JUnit and Spring Test** for automated testing

Spring Data JPA, the PostgreSQL driver, and Docker Compose support are also included. Database auto-configuration and Spring Boot's Docker Compose integration are currently disabled in `src/main/resources/application.properties`, so the application does not currently require PostgreSQL to run.

## Architecture

```text
HTTP request → PlayerController → PlayerService → SoccerApiClient → API-FOOTBALL
```

- **Controller:** validates the incoming player ID and translates outcomes into HTTP responses.
- **Service:** exposes the player-profile operation and currently delegates to the client. Application rules can be added here as features grow.
- **Client:** sends the external request, validates the response envelope and player data, and returns a player or an empty `Optional`.
- **DTOs:** represent player profiles and the external API response envelope.

## Package structure

```text
src/
├── main/
│   ├── java/com/example/soccer_app/
│   │   ├── SoccerAppApplication.java
│   │   ├── client/
│   │   ├── controller/
│   │   ├── dto/
│   │   └── service/
│   └── resources/
│       └── application.properties
└── test/
    ├── java/com/example/soccer_app/
    │   └── ... (automated tests grouped by layer)
    └── resources/
        └── application.properties
```

Maven generates compiled classes and test reports under `target/`. Edit source files under `src/`; generated files can be recreated by the build.

## Getting started

### Prerequisites

- JDK 21 installed and available on your terminal's path.
- An API-FOOTBALL API key for requests to the real provider.
- Internet access for initial Maven dependency downloads and live API requests.

A separate Maven installation is not required because the project includes `mvnw` and `mvnw.cmd`.

### Configure the API key

Set `API_SPORTS_KEY` in the environment used to start the application.

For Bash:

```bash
export API_SPORTS_KEY="your-api-key"
```

For PowerShell:

```powershell
$env:API_SPORTS_KEY = "your-api-key"
```

The main application configuration reads this variable:

```properties
external.soccer-api.base-url=https://v3.football.api-sports.io
external.soccer-api.api-key=${API_SPORTS_KEY}
```

The repository includes [`.env.example`](.env.example) as a configuration template. Copy it to `.env` in the project root and replace the placeholder for `API_SPORTS_KEY` with your API key. If you already have a `.env` file, update it instead of overwriting it.

The local `.env` file is ignored by Git. The current application does not automatically load it, so you still need to export `API_SPORTS_KEY` as shown above or configure your IDE's run configuration to load the variable from `.env`.

### Run the application

From the project root, where `pom.xml` is located:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The application uses port `8080` by default. Spring Security's default configuration requires authentication. Unless overridden, use username `user` and the generated password printed in the startup logs.

## API endpoint

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/players/{id}` | Retrieve a player profile by its positive numeric ID. |


Example request (curl will prompt for the generated password):

```bash
curl --user user http://localhost:8080/api/players/7
```

For an authenticated request, the controller produces these outcomes:

| Status | Meaning |
| --- | --- |
| `200 OK` | A valid player profile was returned. |
| `400 Bad Request` | The player ID is zero or negative. |
| `404 Not Found` | The provider returned a valid response with no matching player. |
| `502 Bad Gateway` | The external request failed or returned invalid data. |

Actual player availability depends on the provider's data and your API access.

## Testing

Run all tests from the project root:

```bash
./mvnw test
```

To run a specific test class, replace `YourTestClass` with its class name:

```bash
./mvnw -Dtest=YourTestClass test
```

Tests live under `src/test/java`, with test configuration under `src/test/resources`. Individual scenarios are described by the test method names.

Client slice tests use `@RestClientTest` and simulated HTTP responses, so they do not require real API access or a real API key. Full application-context tests may require additional database or Docker configuration.

On Windows, replace `./mvnw` with `.\mvnw.cmd` in these commands. Test reports are generated in `target/surefire-reports/`.

## License

This project is licensed under the [MIT License](LICENSE).

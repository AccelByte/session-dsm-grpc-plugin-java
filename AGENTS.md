# session-dsm-grpc-plugin-java

An Extend Override app for the **session DSM** (Dedicated Server Management) written in Java. AGS calls this gRPC server to create and terminate dedicated game server sessions.

This is a template project — clone it, replace the sample logic in the service implementation, and deploy.

## Build & Test

```bash
./gradlew build                      # Build with Gradle
./gradlew test                       # Run tests
docker compose up --build            # Run locally with Docker
./gradlew generateProto              # Regenerate proto code
```

## Architecture

AGS invokes this app's gRPC methods instead of its default logic:

```
Game Client → AGS → [gRPC] → This App → Response → AGS
```

The sample implementation supports three DS provider backends (DEMO, GameLift, GCP) selected via the `DS_PROVIDER` environment variable, each implementing the same create/terminate session interface.

### Key Files

| Path | Purpose |
|---|---|
| `src/main/java/net/accelbyte/session/sessiondsm/Application.java` | Entry point — starts gRPC server, wires interceptors and observability |
| `src/main/java/net/accelbyte/session/sessiondsm/model/ServerServiceType.java` | **Service implementation** — your custom logic goes here |
| `src/main/java/net/accelbyte/session/sessiondsm/service/demo/SessionDsm.java` | **Service implementation** — your custom logic goes here |
| `src/main/proto/session-dsm.proto` | gRPC service definition (AccelByte-provided, do not modify) |
| `docker-compose.yaml` | Local development setup |
| `.env.template` | Environment variable template |

### DS Provider Notes

The `DS_PROVIDER` environment variable selects which backend handles server provisioning: `DEMO` returns mock server info immediately, `GAMELIFT` provisions through AWS GameLift APIs, and `GCP` creates Compute Engine instances. The async variant (`CreateGameSessionAsync`) returns success immediately and updates the session's DS information in the background via the AccelByte Session SDK. Each provider is in its own package under `pkg/server/`.

## Rules

See `.agents/rules/` for coding conventions, commit standards, and proto file policies.

## Environment

Copy `.env.template` to `.env` and fill in your credentials.

| Variable | Description |
|---|---|
| `AB_BASE_URL` | AccelByte base URL (e.g. `https://test.accelbyte.io`) |
| `AB_CLIENT_ID` | OAuth client ID |
| `AB_CLIENT_SECRET` | OAuth client secret |
| `AB_NAMESPACE` | Target namespace |
| `PLUGIN_GRPC_SERVER_AUTH_ENABLED` | Enable gRPC auth (`true` by default) |
| `DS_PROVIDER` | DS provider implementation (`DEMO`, `GAMELIFT`, or `GCP`) |
| `AWS_ACCESS_KEY_ID` | AWS access key (GameLift provider) |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key (GameLift provider) |
| `AWS_REGION` | AWS region (GameLift provider) |
| `GAMELIFT_REGION` | GameLift region (alias for AWS_REGION) |
| `GCP_SERVICE_ACCOUNT_FILE` | GCP service account JSON file path |
| `GCP_PROJECT_ID` | GCP project ID |
| `GCP_NETWORK` | GCP network type |
| `GCP_MACHINE_TYPE` | GCP instance type |
| `GCP_REPOSITORY` | GCP Artifact Registry repository |
| `GCP_RETRY` | GCP instance creation retry count |
| `GCP_WAIT_GET_IP` | Wait time (seconds) to get GCP instance IP |
| `GCP_IMAGE_OPEN_PORT` | Dedicated server open port |

## Dependencies

- [AccelByte Java SDK](https://github.com/AccelByte/accelbyte-java-sdk) (`net.accelbyte.sdk:sdk`) — AGS platform SDK and gRPC plugin utilities

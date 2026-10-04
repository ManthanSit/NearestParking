# Nearest Car Park Service

A Java 27 and Spring Boot 4.1.1 service that returns nearby HDB car parks with live lot availability. It caches the HDB catalogue and latest availability in Redis, converts catalogue SVY21 coordinates to WGS84 for geospatial searches, and exposes a versioned REST API.

## Requirements

- Java 27 (JDK) and Maven 3.9+
- Docker Engine with the Compose plugin for container runs

## Run locally

Start Redis, then run the application. It serves the API, loads the catalogue at startup, and polls availability every minute:

```sh
docker run --rm --name carpark-redis -p 6379:6379 redis:8-alpine
mvn spring-boot:run
```

The application defaults to `localhost:6379`. Override settings with environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | empty | Redis password |
| `REDIS_CONNECT_TIMEOUT` | `2s` | Redis connection timeout |
| `CARPARK_CATALOG_REFRESH_COOLDOWN_SECONDS` | `600` | Server-wide manual catalogue refresh cooldown |
| `AVAILABILITY_POLL_INTERVAL_MS` | `60000` | Availability polling interval |
| `AVAILABILITY_STALE_AFTER` | `120s` | Maximum source timestamp age before readiness reports stale availability |
| `SEARCH_RATE_LIMIT` | `30` | Searches allowed per client IP in one fixed window |
| `SEARCH_RATE_WINDOW_SECONDS` | `60` | Search rate-limit window length |
| `DATA_GOV_SG_CATALOGUE_URL` | Data.gov.sg catalogue endpoint | HDB catalogue source |
| `DATA_GOV_SG_CATALOGUE_RESOURCE_ID` | HDB catalogue resource ID | HDB dataset resource |
| `DATA_GOV_SG_AVAILABILITY_URL` | Data.gov.sg availability endpoint | Availability source |

Endpoints:

- `GET /api/v1/carparks/nearest?latitude=1.3&longitude=103.8&vehicleType=car` — nearby available HDB car parks. `radiusKm` defaults to 3 and accepts values above 0 through 5; `limit` defaults to 3 and accepts 1–10. Vehicle types are `car`, `motorcycle`, and `heavy`. Search is limited to 30 requests per client IP per minute.
- `POST /api/v1/carparks/catalog/refresh` — public manual HDB catalogue refresh, limited by a global 10-minute cooldown.
- `GET /swagger-ui.html` — interactive API documentation
- `GET /openapi.yaml` — versioned OpenAPI contract
- `GET /actuator/health` — aggregate health
- `GET /actuator/health/liveness` — process liveness probe
- `GET /actuator/health/readiness` — traffic readiness probe
- `GET /actuator/info` — application information

Actuator exposes only health and info over HTTP. Readiness details show catalogue status and the Data.gov.sg availability timestamp and age; availability older than `AVAILABILITY_STALE_AFTER` makes readiness return down.

## Run with Docker Compose

```sh
docker compose up --build
```

Compose starts one application and Redis. The application listens on `http://localhost:${SERVER_PORT:-8080}`, loads the catalogue at startup, and fetches availability immediately and then every minute. Redis data is stored in the named `redis-data` volume. To stop the services, run `docker compose down`; add `-v` if you also want to delete Redis data.

For an app-only image build and run:

```sh
docker build -t nearest-car-park:local .
docker run --rm -p 8080:8080 -e REDIS_HOST=host.docker.internal nearest-car-park:local
```

## Build and test

```sh
mvn test
mvn package
```

The Redis rate-limit integration test starts a disposable Redis container and requires Docker:

```sh
mvn -Dtest=RedisRateLimitIntegrationTest test
```

Live Data.gov.sg fetch tests are opt-in and require internet access:

```sh
mvn -Dtest=DataGovSgLiveIntegrationTest -DrunLiveDataGovTests=true test
```

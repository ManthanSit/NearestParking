# Design

## Runtime components

- `app` serves the public HTTP API, loads the HDB catalogue when needed, and retrieves availability immediately at startup, then every 60 seconds.
- `redis` stores a versioned HDB catalogue payload and geospatial index, the latest availability snapshot, refresh cooldown, and search rate-limit counters.

## HDB catalogue

The application checks Redis at startup. If the catalogue is missing, it fetches the HDB Carpark Information dataset. Convert its SVY21 easting/northing values to WGS84 longitude/latitude once, then write a new versioned catalogue payload and Redis GEO index. Change the active version only after both writes complete. Keep the active version if the fetch, parse, or write fails.

`POST /api/v1/carparks/catalog/refresh` refreshes this catalogue on demand. It requires no token. A Redis-backed global cooldown allows one refresh every ten minutes and prevents concurrent upstream fetches.

## Availability

The application calls the Data.gov.sg availability endpoint immediately at startup and then every 60 seconds. Store each complete successful snapshot as one Redis value so publication is atomic. On failure, retain the last successful snapshot unchanged. Return the Data.gov.sg source timestamp so clients can determine its age. Do not retain historical snapshots.

## Search API

`GET /api/v1/carparks/nearest` requires WGS84 `latitude`, `longitude`, and `vehicleType` (`car`, `motorcycle`, or `heavy`). `radiusKm` defaults to 3 and accepts values greater than 0 through 5. `limit` defaults to 3 and accepts integers from 1 through 10.

Search the Redis GEO index within the requested radius and retrieve candidates with Redis's distances sorted ascending. Convert those distances from kilometres to metres and preserve that order while filtering for availability and applying the result limit. Do not recalculate or re-sort distances in the application, and do not apply the result count before filtering, since that could omit available car parks behind full ones. Redis distance is straight-line geographic distance, not road distance.

Responses include the effective radius and result limit, Data.gov.sg availability timestamp, and result fields for car park number, address, distance in metres, vehicle type, available lots, and total lots. No match returns an empty list. Invalid parameters return 400.

## Throttling and health

Use a Redis fixed-window counter keyed by client IP and minute for search requests: 30 allowed requests per minute, then 429 with `Retry-After`. This does not throttle ingestion. Catalogue refresh has its separate ten-minute global cooldown.

Keep health and readiness endpoints available. Readiness requires Redis, a loaded catalogue, and an availability snapshot whose Data.gov.sg source timestamp is no more than two minutes old by default. Readiness details identify availability as `fresh`, `stale`, or `missing` and include its source timestamp and age. The maximum age is configurable.

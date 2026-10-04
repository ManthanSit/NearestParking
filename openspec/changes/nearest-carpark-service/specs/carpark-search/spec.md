# Car park search requirements

## ADDED Requirements

### Requirement: Find available HDB car parks by vehicle and location

The service MUST accept WGS84 latitude and longitude, a vehicle type, an optional radius, and an optional result limit. Vehicle types are car, motorcycle, and heavy vehicle. The default radius MUST be 3 km and the maximum MUST be 5 km. The default result limit MUST be 3 and valid limits MUST be 1 through 10.

#### Scenario: Return nearest available results in Redis distance order

- GIVEN Redis returns car parks within the requested radius sorted by distance
- WHEN the caller requests nearest car parks
- THEN filter by vehicle availability, preserve Redis's ascending distance order, return Redis's distance converted to metres, and apply the result limit

#### Scenario: No available result is in radius

- GIVEN there are no matching available car parks within the requested radius
- WHEN the caller searches
- THEN return a successful response with an empty results list

#### Scenario: Invalid search values

- GIVEN an invalid coordinate, vehicle type, radius, or result limit
- WHEN the caller searches
- THEN return HTTP 400 with a clear validation error

### Requirement: Reuse cached HDB catalogue

The API MUST load the catalogue from Redis on startup and MUST fetch the official HDB catalogue only when Redis has no active catalogue.

#### Scenario: Catalogue exists in Redis

- GIVEN a complete catalogue is already in Redis
- WHEN the API starts
- THEN it MUST use that catalogue without fetching the source dataset

#### Scenario: Catalogue is missing

- GIVEN Redis has no active catalogue
- WHEN the API starts
- THEN it MUST fetch, validate, convert, and publish the official catalogue before becoming ready

### Requirement: Manually refresh the HDB catalogue

The service MUST expose an unauthenticated refresh endpoint. It MUST limit successful refresh attempts globally to one per ten minutes and MUST preserve the last good catalogue if a refresh fails.

#### Scenario: Refresh accepted

- GIVEN the global cooldown has expired
- WHEN the caller posts to the refresh endpoint
- THEN fetch and validate the catalogue and atomically activate the replacement

#### Scenario: Refresh during cooldown

- GIVEN a refresh has been accepted within the last ten minutes
- WHEN another caller posts to the refresh endpoint
- THEN do not call the upstream dataset and return HTTP 429 with `Retry-After`

#### Scenario: Upstream refresh failure

- GIVEN an active catalogue exists
- WHEN an upstream refresh fails or contains invalid data
- THEN retain the active catalogue and report the failure

### Requirement: Poll and retain current availability

The application MUST fetch a complete availability snapshot immediately at startup and every 60 seconds thereafter. The service MUST atomically publish successful snapshots and retain the last successful snapshot after a failed poll. Responses MUST report the timestamp supplied by Data.gov.sg; clients can use that source timestamp to judge the age of the data.

#### Scenario: Poll succeeds

- WHEN a complete valid snapshot is received
- THEN publish its data and source timestamp as the current snapshot

#### Scenario: Poll fails

- GIVEN a successful snapshot exists
- WHEN a poll fails
- THEN keep serving the previous snapshot with its original source timestamp

### Requirement: Report availability freshness in readiness

Readiness MUST report availability as missing, fresh, or stale based on the Data.gov.sg source timestamp. By default, timestamps older than two minutes MUST make readiness down. The maximum age MUST be configurable.

#### Scenario: Availability timestamp is too old

- GIVEN Redis contains a snapshot with a Data.gov.sg timestamp older than the configured maximum age
- WHEN readiness is checked
- THEN readiness is down and its details report availability as stale with the source timestamp

### Requirement: Limit public search traffic

The service MUST allow at most 30 nearest-search requests per client IP per fixed one-minute window. Requests beyond the limit MUST return HTTP 429 and a `Retry-After` header.

#### Scenario: Client exceeds search limit

- GIVEN an IP has made 30 search requests in the current minute
- WHEN it makes another search request during that minute
- THEN reject the request with HTTP 429 without invoking search logic

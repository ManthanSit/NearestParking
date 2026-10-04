# Nearest HDB car park service

## Why

Provide a public API for finding nearby HDB car parks with live availability without making a Data.gov.sg request for every user search.

## What changes

- Cache the HDB location catalogue and latest availability snapshot in Redis.
- Refresh availability every minute in the application process.
- Load the HDB catalogue from Data.gov.sg only when Redis has no catalogue; allow an unauthenticated manual refresh with a ten-minute global cooldown.
- Add a nearest-car-park API with vehicle type, result count, and radius selection.
- Publish the OpenAPI contract and interactive Swagger UI.

## Out of scope

- Historical availability, non-HDB car parks, API keys, cloud deployment, load balancing, and scheduled catalogue refresh.

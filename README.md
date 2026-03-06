# Transporter Assignment on Lanes

Spring Boot service for assigning transporters to lanes with a cap on how many transporters can be used.

## What it does

Given:
- lanes
- transporters with lane-wise quotes
- `maxTransporters`

the service returns an assignment that:
- covers all lanes
- uses at most `maxTransporters`
- minimizes total cost

If two solutions have the same cost, it keeps the one using more transporters (within the limit).

## Stack

- Java 21
- Spring Boot 4
- Maven
- Lombok
- Spring Validation
- SpringDoc OpenAPI

## Endpoints

1. `POST /api/v1/transporters/input`
Stores lanes + transporter quotes in memory.

2. `POST /api/v1/transporters/assignment`
Computes optimized assignment for the stored input.

## Quick run

```bash
mvn clean package
java -jar target/lanes-0.0.1-SNAPSHOT.jar
```

Default port: `9999`  
Swagger UI: `http://localhost:9999/swagger-ui.html`

## Example requests

Load input:

```bash
curl -X POST http://localhost:9999/api/v1/transporters/input \
  -H "Content-Type: application/json" \
  -d '{
    "lanes": [
      {"id": 1, "origin": "Mumbai", "destination": "Delhi"},
      {"id": 2, "origin": "Delhi", "destination": "Bangalore"}
    ],
    "transporters": [
      {
        "id": 1,
        "name": "T1",
        "laneQuotes": [
          {"laneId": 1, "quote": 5000},
          {"laneId": 2, "quote": 6000}
        ]
      },
      {
        "id": 2,
        "name": "T2",
        "laneQuotes": [
          {"laneId": 1, "quote": 4500},
          {"laneId": 2, "quote": 6500}
        ]
      }
    ]
  }'
```

Run optimization:

```bash
curl -X POST http://localhost:9999/api/v1/transporters/assignment \
  -H "Content-Type: application/json" \
  -d '{"maxTransporters": 2}'
```

## Error responses

- `400` invalid request or invalid input data
- `409` assignment requested before input is loaded
- `422` no feasible assignment for provided limit
- `500` unexpected server error

## Tests

```bash
mvn test
```

## Notes

- Storage is in-memory; restart clears data.
- Optimizer is brute force over transporter subsets, so runtime grows quickly with more transporters.

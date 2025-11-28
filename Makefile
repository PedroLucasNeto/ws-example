## Helper make targets to run the stack and tests

.PHONY: up down logs build test-backend test-frontend

up:
	docker compose up -d --build

down:
	docker compose down

logs:
	docker compose logs -f

build:
	docker compose build --no-cache

# Run backend tests using the Maven image (bind mounts the chat folder)
test-backend:
	docker run --rm -v $(PWD)/chat:/app -w /app maven:3.9.6-eclipse-temurin-17 mvn test

# Run frontend tests (if you have any configured in package.json)
test-frontend:
	docker run --rm -v $(PWD):/app -w /app node:18-alpine sh -c "npm ci && npm test"
.PHONY: up build backend-test tests down

up:
	docker compose up --build -d

build:
	docker compose build --no-cache

backend-test:
	docker compose run --rm backend mvn -B test

tests: backend-test

down:
	docker compose down

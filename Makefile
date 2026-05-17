.PHONY: start stop fresh-start start-own-db

# Build the WAR and start MySQL + Tomcat; app at http://localhost:8090
start:
	mvn -q -DskipTests package
	docker compose up -d --wait
	docker compose restart tomcat

# Stop the containers (the database is kept)
stop:
	docker compose down

# Wipe the database, rebuild, and start from a clean slate
fresh-start:
	docker compose down -v
	mvn -q -DskipTests package
	docker compose up -d --wait

# Run against your own MySQL server (configure db.env first; see README)
start-own-db:
	mvn -q -DskipTests package
	docker compose -f docker-compose.own-db.yml up -d

# Threat Intelligence Processing Platform

A distributed microservices-based cybersecurity platform that ingests threat data from AbuseIPDB and AlienVault OTX, extracts and validates Indicators of Compromise (IOCs), scores them using VirusTotal, and stores enriched results in MySQL — all connected through Apache Kafka.


---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Microservices](#microservices)
- [Kafka Topics](#kafka-topics)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [API Keys](#api-keys)
- [Troubleshooting](#troubleshooting)
- 


---

## Overview

Manual threat analysis cannot keep pace with modern attack volumes. This platform automates the full pipeline — from pulling raw data out of threat feeds, all the way through to storing clean, severity-scored IOCs ready to query.

**What it does:**
- Fetches IP and domain threat data from AbuseIPDB and AlienVault OTX
- Extracts and validates IOCs using regex-based parsing
- Deduplicates and filters private or non-routable indicators
- Scores each IOC using VirusTotal (rule-based + API scoring)
- Stores enriched records in MySQL with full audit fields
- Exposes REST endpoints for querying, ranking, and analytics


---

## Architecture

```
External APIs          Kafka Pipeline                    Storage & Analytics
─────────────          ──────────────                    ───────────────────
AbuseIPDB  ──┐                                           ┌── MySQL DB
             ├──► Ingestion ──► [raw-threats] ──► Processing ──► [processed-iocs] ──► Database Service
AlienVault ──┘    Service         (Kafka)          Service                                   │
                                                      │                                      │
                                                      └──► [iocs-to-rank] ──► Ranking ───────┘
                                                                               Service
                                                                            (Severity Scores)
                                                                                  │
                                                                           [ranked-iocs]
                                                                                  │
                                                                           Database Service
                                                                                  │
                                                                           Analytics Service
                                                                                  │
API Gateway (:8080) ◄──────────────── Frontend Dashboard ◄──────────────────────────┘
Eureka Service Registry (:8761)
```

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 + Spring Boot |
| Service Discovery | Spring Cloud Eureka |
| API Gateway | Spring Cloud Gateway |
| Messaging | Apache Kafka 3.x + Zookeeper |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA (Hibernate) |
| HTTP Client | Spring WebFlux (WebClient) |
| Containerisation | Docker + Docker Compose |
| Build Tool | Apache Maven 3.8+ |
| Frontend | HTML + CSS + Vanilla JS |
| Threat Feed 1 | AbuseIPDB REST API |
| Threat Feed 2 | AlienVault OTX REST API |


---

## Microservices

| Service | Port | Responsibility |
|---|---|---|
| `eureka-server` | 8761 | Service discovery and registry |
| `api-gateway` | 8080 | Single entry point, routing, CORS |
| `ingestion-service` | 8081 | Fetches data from AbuseIPDB and AlienVault |
| `extraction-service` | 8082 | Parses JSON, extracts IPs and domains |
| `processing-service` | 8083 | Validates, deduplicates, and re-publishes IOCs |
| `database-service` | 8084 | Stores IOCs in MySQL, exposes REST API |
| `ranking-service` | 8085 | Computes severity scores via rule-based + VirusTotal |
| `analytics-service` | 8086 | Aggregated views and statistics |
| `kafka-producer-service` | 8087 | Manual and programmatic Kafka publishing |

---

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `raw-threats` | Ingestion Service | Processing Service |
| `processed-iocs` | Processing Service | Database Service |
| `iocs-to-rank` | Processing Service | Ranking Service |
| `ranked-iocs` | Ranking Service | Database Service |

---

## Getting Started

### Prerequisites

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org |
| MySQL | 8.0 | https://dev.mysql.com/downloads |
| Apache Kafka | 3.x | https://kafka.apache.org/downloads |
| Docker | Latest | https://www.docker.com |

---

### Option A — Docker Compose (Recommended)

```bash
# 1. Clone the repository
git clone https://github.com/your-username/threat-intel-platform.git
cd threat-intel-platform

# 2. (Optional) Set real API keys
export ABUSEIPDB_API_KEY=your_key_here
export ALIENVAULT_API_KEY=your_key_here
export VIRUSTOTAL_API_KEY=your_key_here

# 3. Start everything
docker-compose up --build
```

Once running, open:
- Frontend dashboard: http://localhost:3000
- Eureka dashboard: http://localhost:8761
- Kafka UI: http://localhost:9090

> First build takes around 5 minutes. Subsequent starts are faster.

---

### Option B — Run Manually

#### Step 1: Set up MySQL

```bash
mysql -u root -p < schema.sql
```

Update credentials in `database-service/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: root
    password: your_password
```

#### Step 2: Start Zookeeper and Kafka

```bash
# Terminal 1 — Zookeeper
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties

# Terminal 2 — Kafka broker
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties

# Terminal 3 — Create topics (wait 10 seconds after Kafka starts)
chmod +x create-kafka-topics.sh
./create-kafka-topics.sh
```

#### Step 3: Build all services

```bash
mvn clean package -DskipTests
```

#### Step 4: Start services in order

> Always start Eureka first — other services register with it on startup.

```bash
# Terminal 1 — Eureka (wait 10 seconds before starting others)
java -jar eureka-server/target/eureka-server-1.0.0.jar

# Terminal 2 — API Gateway
java -jar api-gateway/target/api-gateway-1.0.0.jar

# Terminal 3 — Ingestion Service
java -jar ingestion-service/target/ingestion-service-1.0.0.jar

# Terminal 4 — Extraction Service
java -jar extraction-service/target/extraction-service-1.0.0.jar

# Terminal 5 — Processing Service
java -jar processing-service/target/processing-service-1.0.0.jar

# Terminal 6 — Database Service
java -jar database-service/target/database-service-1.0.0.jar

# Terminal 7 — Ranking Service
java -jar ranking-service/target/ranking-service-1.0.0.jar

# Terminal 8 — Analytics Service
java -jar analytics-service/target/analytics-service-1.0.0.jar

# Terminal 9 — Kafka Producer Service
java -jar kafka-producer-service/target/kafka-producer-service-1.0.0.jar
```

Or use the startup script:

```bash
chmod +x start-all.sh && ./start-all.sh
```

#### Step 5: Open the frontend

```
threat-intel-platform/frontend/src/index.html
```

Or with live-server:

```bash
cd frontend/src
npx live-server --port=3000
```

---

## API Endpoints

All requests can go through the API Gateway at `http://localhost:8080`.

### Ingestion

```bash
# Trigger ingestion from all sources
curl -X POST "http://localhost:8081/api/ingest/trigger?source=all"
```

### IOCs

```bash
# Get all IOCs (paginated)
curl "http://localhost:8084/api/ioc?page=0&size=10"

# Get statistics
curl "http://localhost:8084/api/ioc/stats"

# Get high-severity IOCs
curl "http://localhost:8084/api/ioc/high-severity?minScore=7.0"
```

### Ranking

```bash
# Rank a specific IOC
curl "http://localhost:8085/api/rank/ioc?value=185.220.101.5&type=IP_ADDRESS"
```

### Kafka Producer

```bash
# Manually publish an IOC to Kafka
curl -X POST "http://localhost:8087/api/kafka/publish-ioc?iocValue=1.2.3.4&iocType=IP_ADDRESS"
```

### Through API Gateway

```bash
curl "http://localhost:8080/api/ioc/stats"
curl "http://localhost:8080/api/analytics/summary"
```

---

## API Keys

The system runs in demo mode without API keys — mock data loads automatically.
To use real threat intelligence, register and set the following:

| Provider | Register At | Environment Variable |
|---|---|---|
| AbuseIPDB | https://www.abuseipdb.com | `ABUSEIPDB_API_KEY` |
| AlienVault OTX | https://otx.alienvault.com | `ALIENVAULT_API_KEY` |
| VirusTotal | https://www.virustotal.com | `VIRUSTOTAL_API_KEY` |

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Services not showing in Eureka | Wait 30–60 seconds after starting — Eureka has heartbeat delays |
| MySQL connection refused | Ensure MySQL is running on port 3306 with correct credentials |
| Kafka connection refused | Make sure Zookeeper is started before Kafka |
| `ClassNotFoundException` | Rebuild with `mvn clean package -DskipTests` |
| Port already in use | Run `lsof -ti:8081 \| xargs kill -9` (replace port as needed) |
| No IOCs in database | Trigger manual ingestion via frontend or curl |

---



---

## Project Structure

```
threat-intel-platform/
├── analytics-service/
├── api-gateway/
├── database-service/
├── eureka-server/
├── extraction-service/
├── frontend/
├── ingestion-service/
├── kafka-producer-service/
├── processing-service/
├── ranking-service/
├── docker-compose.yml
├── pom.xml
├── schema.sql
└── README.md
```

---


**Course:** COMP-370 – Software Construction and Development  
**Institution:** PAF-IAST, School of Computing Sciences, Mang, Haripur  
**Instructor:** Dr. Malik Nabeel Ahmed Awan  

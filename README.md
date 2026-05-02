# 🛡 Threat Intelligence Platform

## COMP-370 – Software Construction and Development

### Complex Computing Problem (CCP)

**PAF-IAST | Instructor: Dr. Malik Nabeel Ahmed Awan**

\---

## 📐 Architecture Overview

This platform implements a **distributed microservices-based cybersecurity system** that ingests,
processes, ranks, and serves Indicators of Compromise (IOCs).

```
External APIs          Kafka Pipeline           Storage \\\\\\\& Analytics
─────────────          ──────────────           ───────────────────
AbuseIPDB  ──┐                                  ┌── MySQL DB
             ├──► Ingestion ──► \\\\\\\[raw-threats] ──► Processing ──► \\\\\\\[processed-iocs] ──► Database Service
AlienVault ──┘    Service        (Kafka)          Service        \\\\\\\[iocs-to-rank]        │
                                                                                        ├──► Ranking Service
                                                                                        │    (Severity Scores)
                                                                                        │
                                                                                        └──► Analytics Service
                                                                                             │
API Gateway (port 8080) ◄── Frontend Dashboard ◄─────────────────────────────────────────────┘
Eureka Service Registry (port 8761)
```

## 🧩 Microservices

|Service|Port|Responsibility|
|-|-|-|
|Eureka Server|8761|Service discovery \& registry|
|API Gateway|8080|Single entry point, routing, CORS|
|Ingestion Service|8081|Fetches data from AbuseIPDB \& AlienVault|
|Extraction Service|8082|Parses JSON, extracts IPs \& domains|
|Processing Service|8083|Consumes raw-threats, validates, re-publishes|
|Database Service|8084|Stores IOCs in MySQL, exposes REST API|
|Ranking Service|8085|Computes severity scores (rule-based + VirusTotal)|
|Analytics Service|8086|Aggregated views \& stats|
|Kafka Producer Service|8087|Manual/programmatic Kafka publishing|

## 📦 Kafka Topics

|Topic|Producer|Consumer|
|-|-|-|
|`raw-threats`|Ingestion Service|Processing Service|
|`processed-iocs`|Processing Service|Database Service|
|`iocs-to-rank`|Processing Service|Ranking Service|
|`ranked-iocs`|Ranking Service|Database Service|

\---

## 🚀 Setup Guide

### Prerequisites

Make sure these are installed:

|Tool|Version|Download|
|-|-|-|
|Java JDK|17+|https://adoptium.net/|
|Maven|3.8+|https://maven.apache.org/|
|MySQL|8.0|https://dev.mysql.com/downloads/|
|Apache Kafka|3.x|https://kafka.apache.org/downloads|
|Docker (optional)|Latest|https://www.docker.com/|

\---

### 🐳 Option A: Run with Docker Compose (Easiest)

```bash
# 1. Navigate to project root
cd threat-intel-platform

# 2. (Optional) Set real API keys
export ABUSEIPDB\\\\\\\_API\\\\\\\_KEY=your\\\\\\\_key\\\\\\\_here
export ALIENVAULT\\\\\\\_API\\\\\\\_KEY=your\\\\\\\_key\\\\\\\_here

# 3. Start everything
docker-compose up --build

# 4. Open frontend
open http://localhost:3000

# 5. Open Eureka dashboard
open http://localhost:8761

# 6. Open Kafka UI
open http://localhost:9090
```

> ⚠️ First build takes \\\\\\\~5 minutes. Subsequent starts are faster.

\---

### 🔧 Option B: Run Manually (Step by Step)

#### Step 1: Start MySQL

```sql
-- Using MySQL CLI or Workbench:
mysql -u root -p < init.sql
-- OR let Spring JPA auto-create the tables (ddl-auto: update)
```

Make sure MySQL is running on port 3306. Update credentials in:
`database-service/src/main/resources/application.yml`

```yaml
spring.datasource.username: root
spring.datasource.password: root   # change this
```

\---

#### Step 2: Start Zookeeper and Kafka

```bash
# Terminal 1 - Start Zookeeper
$KAFKA\\\\\\\_HOME/bin/zookeeper-server-start.sh $KAFKA\\\\\\\_HOME/config/zookeeper.properties

# Terminal 2 - Start Kafka broker
$KAFKA\\\\\\\_HOME/bin/kafka-server-start.sh $KAFKA\\\\\\\_HOME/config/server.properties

# Terminal 3 - Create topics (wait 10 seconds after Kafka starts)
chmod +x create-kafka-topics.sh
./create-kafka-topics.sh
```

\---

#### Step 3: Build All Services

```bash
cd threat-intel-platform

# Build everything at once (from root pom)
mvn clean package -DskipTests

# Or build individual service
cd eureka-server \\\\\\\&\\\\\\\& mvn clean package -DskipTests \\\\\\\&\\\\\\\& cd ..
```

\---

#### Step 4: Start Services in Order

Open a separate terminal for each service, OR use the script:

```bash
# Option A: Use startup script
chmod +x start-all.sh
./start-all.sh

# Option B: Manual (recommended for debugging - one terminal per service)

# Terminal 1: Eureka (start FIRST, wait 10 seconds)
java -jar eureka-server/target/eureka-server-1.0.0.jar

# Terminal 2: API Gateway
java -jar api-gateway/target/api-gateway-1.0.0.jar

# Terminal 3: Ingestion Service
java -DABUSEIPDB\\\\\\\_API\\\\\\\_KEY=your\\\\\\\_key -jar ingestion-service/target/ingestion-service-1.0.0.jar

# Terminal 4: Extraction Service
java -jar extraction-service/target/extraction-service-1.0.0.jar

# Terminal 5: Processing Service
java -jar processing-service/target/processing-service-1.0.0.jar

# Terminal 6: Database Service
java -jar database-service/target/database-service-1.0.0.jar

# Terminal 7: Ranking Service
java -jar ranking-service/target/ranking-service-1.0.0.jar

# Terminal 8: Analytics Service
java -jar analytics-service/target/analytics-service-1.0.0.jar

# Terminal 9: Kafka Producer Service
java -jar kafka-producer-service/target/kafka-producer-service-1.0.0.jar
```

> ⚡ \\\\\\\*\\\\\\\*Always start Eureka first!\\\\\\\*\\\\\\\* Other services register with it on startup.

\---

#### Step 5: Open the Frontend

Just open the HTML file in your browser:

```
threat-intel-platform/frontend/src/index.html
```

Or with live-server:

```bash
cd frontend/src
npx live-server --port=3000
```

\---

## 🔑 API Keys 

The system runs in **demo mode** without API keys (uses mock data).
To use real threat intelligence:

### AbuseIPDB

1. Register at https://www.abuseipdb.com/
2. Generate API key
3. Set: `ABUSEIPDB\\\\\\\_API\\\\\\\_KEY=your\\\\\\\_key` in environment

### AlienVault OTX

1. Register at https://otx.alienvault.com/
2. Get API key from your profile
3. Set: `ALIENVAULT\\\\\\\_API\\\\\\\_KEY=your\\\\\\\_key`

### VirusTotal (for advanced ranking)

1. Register at https://www.virustotal.com/
2. Set: `VIRUSTOTAL\\\\\\\_API\\\\\\\_KEY=your\\\\\\\_key`

\---

## 🧪 Testing the API

### Trigger Ingestion

```bash
curl -X POST "http://localhost:8081/api/ingest/trigger?source=all"
```

### Get All IOCs

```bash
curl "http://localhost:8084/api/ioc?page=0\\\\\\\&size=10"
```

### Rank an IOC

```bash
curl "http://localhost:8085/api/rank/ioc?value=185.220.101.5\\\\\\\&type=IP\\\\\\\_ADDRESS"
```

### Get Statistics

```bash
curl "http://localhost:8084/api/ioc/stats"
```

### Get High-Severity IOCs

```bash
curl "http://localhost:8084/api/ioc/high-severity?minScore=7.0"
```

### Manually Publish to Kafka

```bash
curl -X POST "http://localhost:8087/api/kafka/publish-ioc?iocValue=1.2.3.4\\\\\\\&iocType=IP\\\\\\\_ADDRESS"
```

### Through API Gateway

```bash
curl "http://localhost:8080/api/ioc/stats"
curl "http://localhost:8080/api/analytics/summary"
```

\---

\---

## 🔍 Troubleshooting

|Problem|Solution|
|-|-|
|Services not showing in Eureka|Wait 30-60 seconds after starting — Eureka has heartbeat delays|
|MySQL connection refused|Ensure MySQL is running on port 3306 with correct credentials|
|Kafka connection refused|Make sure Zookeeper is started before Kafka|
|`java.lang.ClassNotFoundException`|Rebuild with `mvn clean package -DskipTests`|
|Port already in use|Kill the process: `lsof -ti:8081 \| xargs kill -9`|
|No IOCs in database|Trigger manual ingestion via the frontend or curl command above|

\---

## 📊 CCP Requirements Mapping

|CCP Requirement|Implementation|
|-|-|
|Microservices architecture|9 Spring Boot services + Eureka + Gateway|
|Apache Kafka|4 topics: raw-threats, processed-iocs, iocs-to-rank, ranked-iocs|
|MySQL persistent storage|JPA entities + Repository (IocRecord)|
|AbuseIPDB integration|`AbuseIPDBClient.java` — fetches blacklist|
|AlienVault integration|`AlienVaultClient.java` — fetches pulses|
|IOC extraction (IPs \& domains)|`ExtractionController.java`, `IocProcessorService.java`|
|IOC severity ranking|`RankingService.java` — rule-based + VirusTotal|
|Event-driven architecture|All inter-service comms via Kafka topics|
|Eureka service discovery|All services register and discover via Eureka|
|REST API (6-9 APIs)|Each service exposes REST endpoints|
|Frontend|Full dashboard: dashboard, IOC table, ingestion, analytics|
|Scalable \& loosely coupled|Each service independently deployable|

\---

*COMP-370 | PAF-IAST School of Computing Sciences | Spring 2026*


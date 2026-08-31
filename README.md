# mazadHub — Electronic Auction System

**Student:** Malak Masarwa  
**Course:** Workshop in Advanced Programming in Java  
**Repository:** https://github.com/mlkmas/mazadhub

An electronic auction marketplace (eBay-style) where registered users list items
for sale and bid on items belonging to others. The system manages the full
auction lifecycle: listing, competing concurrent bids, automatic proxy bidding,
"Buy Now", and automatic closing with winner determination.

---

## 1. Technologies

| Technology | Where it is used |
|---|---|
| **JSF (Jakarta Faces)** | 7 XHTML screens + CDI managed beans (`com.mazadhub.web`) |
| **JPA / Hibernate** | 5 entities mapped to MySQL, repositories (`com.mazadhub.domain`, `.repository`) |
| **JMS** | Publish/subscribe auction events: topic publisher + message-driven listener (`com.mazadhub.notification`) |
| **JAX-RS (RESTful Web Services)** | External JSON API under `/api` (`com.mazadhub.api`) |
| **EJB Timer Service** | Scheduled auction closing (`AuctionScheduler`) |
| **Maven** | Dependency management and build |
| **MySQL 8** | Relational database |

Runtime: **Jakarta EE 10**, **JDK 17**, **GlassFish 7.1.1**.

---

## 2. Requirements

Install before running:

1. **JDK 17** (Eclipse Temurin recommended)
2. **Maven 3.9+**
3. **MySQL Server 8** (running on port 3306)
4. **GlassFish 7.1.1** — Jakarta EE 10 Full Platform

---

## 3. Installation

### 3.1 Create the database

Log in to MySQL and run:

```sql
CREATE DATABASE mazadhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mazadhub'@'localhost' IDENTIFIED BY 'Mazad123';
GRANT ALL PRIVILEGES ON mazadhub.* TO 'mazadhub'@'localhost';
FLUSH PRIVILEGES;
```

The tables are created automatically by Hibernate on first deployment
(`hibernate.hbm2ddl.auto=update`).

### 3.2 Configure GlassFish

Copy the MySQL driver into the server, then create the resources:

```bash
# 1. MySQL JDBC driver -> GlassFish
copy "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" ^
     "C:\glassfish7\glassfish\domains\domain1\lib\"

cd C:\glassfish7\bin
.\asadmin start-domain

# 2. JDBC connection pool + datasource (name must match persistence.xml)
.\asadmin create-jdbc-connection-pool --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource ^
  --restype javax.sql.DataSource ^
  --property "user=mazadhub:password=Mazad123:serverName=localhost:portNumber=3306:databaseName=mazadhub:useSSL=false:allowPublicKeyRetrieval=true" ^
  MazadHubPool

.\asadmin ping-connection-pool MazadHubPool     # must succeed
.\asadmin create-jdbc-resource --connectionpoolid MazadHubPool jdbc/mazadhub

# 3. JMS resources (live price updates)
.\asadmin create-jms-resource --restype jakarta.jms.ConnectionFactory jms/mazadhubFactory
.\asadmin create-jms-resource --restype jakarta.jms.Topic --property Name=auctionTopic jms/auctionTopic
```

### 3.3 Build and deploy

```bash
cd mazadhub
mvn clean package
cd C:\glassfish7\bin
.\asadmin deploy "path\to\mazadhub\target\mazadhub.war"
```

Open **http://localhost:8080/mazadhub/**

> To redeploy after a change use
> `asadmin redeploy --name mazadhub "...\target\mazadhub.war"`.

### 3.4 Load demo data (optional but recommended)

Run `seed_demo_data.sql` in HeidiSQL (or any MySQL client). It creates users
with correctly hashed passwords, 6 categories, 9 items and sample bid history.

| Login | Password | Role |
|---|---|---|
| `admin` | `Admin1234` | Administrator |
| `malak.m` | `Demo1234` | User |
| `david.c` | `Demo1234` | User |
| `noa.b` | `Demo1234` | User |

---

## 4. Running the tests

```bash
mvn test
```

**159 automated tests**, no database or server required (the service tests use
in-memory repository doubles). The suite has four layers:

| Layer | What it does |
|---|---|
| Unit tests | Pricing ladder, validator, proxy engine, password hashing, services |
| Parameterized tests | ~48 numeric cases covering every price-tier boundary |
| Data-driven scenario tests | 15 script files in `src/test/resources/scenarios/` replayed and compared against expected results |
| Randomised invariant (fuzz) tests | 2,400 random bids over fixed seeds, checking 5 invariants after every bid |

Adding a scenario test needs no Java — just another `.txt` file, e.g.:

```
START 100
AUTO alice 30000
EXPECT 100 alice
BID bob 20000
EXPECT 20100 alice
```

Run one suite only:

```bash
mvn test -Dtest=ScenarioFileTest
```

---

## 5. Project structure

```
src/main/java/com/mazadhub/
  domain/         JPA entities (User, Category, Item, Bid, AutoBid)
  repository/     Data access (JPQL, locking)
  pricing/        Minimum-increment ladder
  bidding/        Proxy bid engine + bid validator (pure logic)
  service/        Business logic (UserService, ItemService, BiddingService,
                  AuctionCloser, AuctionScheduler)
  security/       PBKDF2 password hashing
  notification/   NotificationPort + JMS publisher + message-driven listener
  api/            JAX-RS resources and DTOs
  web/            JSF managed beans
src/main/webapp/  XHTML screens, template, CSS, JS
src/test/         159 tests + scenario files
```

---

## 6. Troubleshooting

| Problem | Cause and fix |
|---|---|
| Deploy fails: *Unable to build Hibernate SessionFactory* / "Communications link failure" | MySQL is not running. Start the service, then deploy again. |
| `ping-connection-pool` fails: *Public Key Retrieval is not allowed* | Recreate the pool including `allowPublicKeyRetrieval=true`. |
| Deploy fails: *jdbc/mazadhub not found* | The JDBC resource name must exactly match `<jta-data-source>` in `persistence.xml`. |
| *Application with name [mazadhub] is not deployed* | Use `deploy` instead of `redeploy`. |
| Page shows old styling | Hard-reload the browser (Ctrl+Shift+R). |
| Deploy fails mentioning JMS | Create the two JMS resources (§3.2), or comment out the `<alternatives>` block in `WEB-INF/beans.xml` to fall back to logging. |

**Start-up order matters:** MySQL first, then GlassFish, then deploy.
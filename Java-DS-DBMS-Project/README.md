# Java-DS-DBMS Project — Semester 2

A Java-based **Mini Log Monitor** project combining **Java, Data Structures, Multithreading, and MySQL/DBMS** concepts. The application ingests server log files concurrently, parses them into structured records, maintains in-memory indexes, stores records in MySQL, supports searches, and generates statistical reports.

## Project Overview

The application is designed around a producer–consumer workflow:

1. Multiple log files are read concurrently by producer threads.
2. Raw log lines are placed into a `LinkedBlockingQueue`.
3. A parser worker consumes the queue, validates/parses each line, and creates `LogRecords` objects.
4. Parsed records are stored in an in-memory data-structure layer (`HashMap`, `TreeMap`, and lists).
5. Records are also persisted to MySQL through JDBC.
6. Users can search logs and generate statistical reports from the console.

## Main Features

- Load all `.log` files from a directory.
- Select individual log files or ranges of files to load.
- Concurrent log ingestion using Java threads.
- Producer–consumer communication with `LinkedBlockingQueue`.
- Parsing of structured log records using `LocalDateTime`.
- In-memory record storage using `HashMap`.
- Keyword/exception indexing using `HashMap<String, List<Integer>>`.
- Time-based exception indexing using `TreeMap<LocalDateTime, List<Integer>>`.
- Search by exception type and severity.
- Search log messages by keyword.
- MySQL persistence using JDBC.
- Search-history auditing through the database.
- Statistical reports by severity and exception type.
- Top exception reporting by server zone.
- Export of statistical reports to `report.txt`.

## Technologies Used

- **Java** (modern switch expressions and standard concurrency/collection APIs)
- **JDBC**
- **MySQL**
- **MySQL Connector/J**
- `HashMap`, `TreeMap`, `ArrayList`, `List`, `Set`
- `LinkedBlockingQueue`
- Java `Thread`
- `LocalDateTime`
- IntelliJ IDEA project structure

## Project Structure

```text
Java-DS-DBMS-Project/
├── Log Files/
│   ├── application.log
│   ├── server1.log
│   ├── server2.log
│   ├── server3.log
│   ├── server4.log
│   └── server5.log
├── src/
│   ├── DBMS/
│   │   ├── AnalyticsService.java
│   │   └── DBManager.java
│   ├── DS/
│   │   └── InMemoryIndex.java
│   ├── JAVA_2/
│   │   ├── AppMain.java
│   │   ├── LogRecords.java
│   │   ├── ParserWorker.java
│   │   ├── ProducerIngestTask.java
│   │   └── SearchService.java
│   └── Main.java
└── .gitignore
```

Generated IntelliJ/build output (`.idea/` and `out/`) and the bundled MySQL Connector/J JAR are intentionally not included in this repository because they are IDE/build artifacts or external dependencies. Use a dependency manager or add the connector JAR to the project classpath when compiling manually.

## Log Format

The parser expects each log line to contain **7 pipe-separated fields** in this order:

```text
TIMESTAMP|SOURCE|ZONE|SEVERITY|EXCEPTION_TYPE|MESSAGE|STACK_TRACE
```

Example shape:

```text
2026-01-01T10:15:30|server1|North|ERROR|SQLException|Database connection failed|...
```

The parser converts the timestamp to `LocalDateTime` and creates a `LogRecords` object.

## Database Requirements

The Java application connects to MySQL using the following default configuration in `DBManager.java`:

```text
Host: localhost
Port: 3306
Database: log_monitor
User: root
Password: empty by default
```

The code expects these database tables/columns to exist:

- `log_sources`
- `log_events`
- `search_audit`
- `error_types`
- `error_daily_stats`

The exact SQL schema is not included in the supplied project archive, so the database must be prepared separately before running the application.

> **Security note:** Do not commit real database passwords or production credentials. The current source contains local-development connection settings and should be configured appropriately for your environment.

## Running the Project

### 1. Configure MySQL

Create a MySQL database named `log_monitor` and create the tables required by the Java code. Make sure MySQL is running on `localhost:3306`.

### 2. Configure the log-file path

`AppMain.java` currently contains a machine-specific Windows path:

```java
public static String filePath = "C:\\javaprogram\\Java-DS-DBMS Project SEM-2\\Log Files\\";
```

Change this to the location of the repository's `Log Files` directory on your computer before running the application.

### 3. Add MySQL Connector/J

Add a compatible MySQL Connector/J dependency to the project classpath. The original archive contained `mysql-connector-j-9.7.0.jar`, but the binary JAR is intentionally not committed here.

### 4. Run

The primary application entry point is:

```text
src/JAVA_2/AppMain.java
```

Run `AppMain.main()` from IntelliJ IDEA or your Java build environment.

## Console Menu

The application provides these main operations:

```text
1. Load logs from file
2. Show all logs in memory
3. Search
4. Show statistical report
5. Show search history
6. Save report to file
0. Exit
```

Search supports:

- Exception type
- Severity (`INFO`, `WARNING`, `ERROR`, `FATAL`)

Reports support:

- Count by severity
- Count by keyword in messages
- Top exception types for a selected zone

## Data Structures and Concepts Demonstrated

### HashMap
Used for the main `recordStore` and keyword indexing, providing fast key-based access to records/index entries.

### TreeMap
Used for time-based exception indexing, maintaining timestamps in sorted order.

### ArrayList
Used for collections of event IDs and search results.

### LinkedBlockingQueue
Provides thread-safe communication between log-reading producer threads and the parser worker.

### Multithreading
Each selected log file gets a producer thread. A parser thread consumes the resulting queue and processes records.

### JDBC
`DBManager` handles database connectivity, inserts, search auditing, and database queries used by analytics.

## Architecture

```text
Log Files
   │
   ▼
Producer Threads (one per file)
   │
   ▼
LinkedBlockingQueue<String>
   │
   ▼
ParserWorker
   ├──────────────► InMemoryIndex
   │                  ├─ HashMap record store
   │                  ├─ Keyword index
   │                  └─ TreeMap time index
   │
   └──────────────► DBManager / MySQL
                         ├─ log_sources
                         ├─ log_events
                         └─ search_audit

SearchService ─────────► InMemoryIndex + DB audit
AnalyticsService ──────► MySQL reports
```

## Important Implementation Notes

- The producer threads use an `END` marker so the parser knows when all producers have finished.
- Event IDs are initially generated by `ParserWorker`; database-generated IDs can subsequently update the record ID.
- The application prevents loading logs more than once during a single run using the `isLoaded` flag.
- Invalid log lines are reported and skipped rather than terminating the entire parsing process.
- `report.txt` is generated in the application's working directory when the save-report option is used.

## Known Limitations / Improvements

Some areas could be improved in a future version:

- Replace the hard-coded Windows log path with a command-line argument or configuration file.
- Move database credentials to environment variables or a configuration file outside version control.
- Add a database schema/SQL migration script.
- Use try-with-resources consistently for JDBC statements and result sets.
- Improve error handling when the database connection is unavailable.
- Add automated unit tests for parsing, indexing, searching, and selection parsing.
- Consider Maven or Gradle for dependency management.
- Improve keyword tokenization and normalization for punctuation/case variations.
- Add a proper GUI or web interface if the project is extended beyond the console application.

## Academic Context

This repository contains a Semester 2 project demonstrating the integration of **Java programming, Data Structures, Multithreading, and DBMS/JDBC** in one practical log-monitoring application.

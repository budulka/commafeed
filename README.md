## Usage

### Public instance

A free public instance is available at https://www.commafeed.com.

It has no ads, no tracking, and your data is never exploited or sold to third parties. The service is funded entirely through donations.
However, this public instance does have a few limitations compared to self-hosted setups, outlined [here](https://github.com/Athou/commafeed/discussions/1567).

### Docker

Docker is the easiest way to get started with self-hosted CommaFeed.

Docker images are built automatically and are available at https://hub.docker.com/r/athou/commafeed

### Cloud hosting

[PikaPods](https://www.pikapods.com) offers 1-click cloud hosting solutions starting at $1/month with a free $5
welcome credit and officially supports CommaFeed.
PikaPods shares 20% of the revenue back to CommaFeed.

[![PikaPods](https://www.pikapods.com/static/run-button.svg)](https://www.pikapods.com/pods?run=commafeed)

### Download a precompiled package

Go to the [release page](https://github.com/Athou/commafeed/releases) and download the latest version for your operating
system and database of choice.

There are two types of packages:

- The `linux-x86_64`, `linux-aarch_64` and `windows-x86_64` packages are compiled natively and contain an executable that can be run
  directly.
- The `jvm` package is a zip file containing all `.jar` files required to run the application. This package works on all
  platforms but requires a JRE and is started with `java -jar quarkus-run.jar`.

If available for your operating system, the native package is recommended because it has a faster startup time and lower
memory usage.

### Build from sources

    ./mvnw clean package [-P<database> [-Pnative]] [-DskipTests]

- `<database>` can be one of `h2`, `postgresql`, `mysql` or `mariadb`. The default is `h2`.
- `-Pnative` compiles the application to native code. This requires either GraalVM to be installed (`GRAALVM_HOME` environment
  variable pointing to a GraalVM installation) or a container environment to be available (docker/podman/...).
- `-DskipTests` to speed up the build process by skipping tests.

When the build is complete:

- a zip containing all jars required to run the application is located at
  `commafeed-server/target/commafeed-<version>-<database>-jvm.zip`. Extract it and run the application with
  `java -jar quarkus-run.jar`
- if you used the native profile, the executable is located at
  `commafeed-server/target/commafeed-<version>-<database>-<platform>-<arch>-runner[.exe]`

### Distribution packages

- Arch Linux users can use [the CommaFeed package on AUR](https://aur.archlinux.org/pkgbase/commafeed), which builds native binaries with GraalVM for all supported databases.

## Configuration

CommaFeed doesn't require any configuration to run with its embedded database (H2). The database file will be stored in
the `data` directory of the current directory.

To use a different database, you will need to configure the following properties:

- `quarkus.datasource.jdbc.url`
    - e.g. for H2: `jdbc:h2:./data/db;DEFRAG_ALWAYS=TRUE`
    - e.g. for PostgreSQL: `jdbc:postgresql://localhost:5432/commafeed`
    - e.g. for MySQL:
      `jdbc:mysql://localhost/commafeed?autoReconnect=true&failOverReadOnly=false&maxReconnects=20&rewriteBatchedStatements=true&timezone=UTC`
    - e.g. for MariaDB:
      `jdbc:mariadb://localhost/commafeed?autoReconnect=true&failOverReadOnly=false&maxReconnects=20&rewriteBatchedStatements=true&timezone=UTC`
- `quarkus.datasource.username`
- `quarkus.datasource.password`

There are multiple ways to configure CommaFeed:

- a `config/application.properties` [properties](https://en.wikipedia.org/wiki/.properties) file relative to the working
  directory (keys in kebab-case)
- Command line arguments each prefixed with `-D` (keys in kebab-case)
- Environment variables (keys in UPPER_CASE)
- a `.env` file in the working directory (keys in UPPER_CASE)

When in doubt, the properties file is recommended because CommaFeed will be able to warn about invalid properties and typos.

All [CommaFeed settings](https://athou.github.io/commafeed/documentation) are optional and have sensible default values.

When logging in, credentials are stored in an encrypted cookie. The encryption key is randomly generated at startup,
meaning that you will have to log back in after each restart of the application. To prevent this, you can set the
`quarkus.http.auth.session.encryption-key` property to a fixed value (min. 16 characters).
All other Quarkus settings can be found [here](https://quarkus.io/guides/all-config).

When started, the server will listen on http://localhost:8082.

### Updates

When CommaFeed is up and running, you can subscribe to [this feed](https://github.com/Athou/commafeed/releases.atom) to be notified of new releases.

### Memory management

The Java Virtual Machine (JVM) is rather greedy by default and will not release unused memory to the
operating system. This is because acquiring memory from the operating system is a relatively expensive operation.
This can be problematic on systems with limited memory.

#### Hard limit (`native` and `jvm` packages)

The JVM can be configured to use a maximum amount of memory with the `-Xmx` parameter.
For example, to limit the JVM to 256MB of memory, use `-Xmx256m`.

#### Dynamic sizing (`jvm` package)

In addition to the previous setting, the JVM can be configured to release unused memory to the operating system with the
following parameters:

    -Xms20m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:-ShrinkHeapInSteps -XX:G1PeriodicGCInterval=10000 -XX:-G1PeriodicGCInvokesConcurrent -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10

See [here](https://docs.oracle.com/en/java/javase/17/gctuning/garbage-first-g1-garbage-collector1.html)
and [here](https://docs.oracle.com/en/java/javase/17/gctuning/factors-affecting-garbage-collection-performance.html) for
more
information.

#### OpenJ9 (`jvm` package)

The [OpenJ9](https://eclipse.dev/openj9/) JVM is a more memory-efficient alternative to the HotSpot JVM, at the cost of
slightly slower throughput.

IBM provides precompiled binaries for OpenJ9
named [Semeru](https://developer.ibm.com/languages/java/semeru-runtimes/downloads/).
This is the JVM used in
the [Docker image](https://github.com/Athou/commafeed/blob/master/commafeed-server/src/main/docker/Dockerfile.jvm).

## My AI Workflow

For this project, I used a combination of AI agents:
- **GitHub Copilot** was used for **Level 1 (Saved Entry Notes)** to quickly scaffold the vertical slice (Entity, DAO, Service, REST) following existing patterns.
- **Junie** (powered by Gemini-3-Flash) was used for **Level 2 (LLM 'Rewrite This Entry')** to handle the more complex integration with external APIs, error mapping, and architectural refinements.

My workflow followed these principles:

1. **Plan-First Approach**: Before writing any code, I generated and refined implementation plans in the `specs/` folder. This ensured that both the AI and the user were aligned on the architectural direction.
2. **Context Management**: Instead of pasting the whole codebase, I used tools to explore the project structure and selectively read relevant files (Service, DAO, Model, Resource) to build a mental map of the project's patterns.
3. **Architectural Alignment**: I strictly followed CommaFeed's layered architecture (REST -> Service -> DAO -> JPA Entity). When the AI suggested bypassing a layer (e.g., calling DAO directly from REST), I intervened to maintain consistency.
4. **Iterative Refinement**: I used unit tests to verify each vertical slice immediately after implementation. This caught compilation errors and logic bugs early.
5. **Decision Logging**: I maintained `DECISIONS.md` to track where I had to override or redirect the AI's proposals, providing a clear audit trail of the development process.

## New Endpoints - Samples

### Level 1: Saved Entry Notes

**Create or update a note:**
```bash
curl -u admin:admin -X POST http://localhost:8083/rest/note \
  -H "Content-Type: application/json" \
  -d '{"entryId": 123, "text": "This is a great article!", "rating": 5}'
```
Response: `201 Created` or `200 OK` with JSON:
```json
{
  "id": 1,
  "entryId": 123,
  "text": "This is a great article!",
  "rating": 5,
  "created": "2026-07-30T13:00:00Z",
  "updated": "2026-07-30T13:00:00Z"
}
```

**List user notes:**
```bash
curl -u admin:admin "http://localhost:8083/rest/note?limit=10"
```
Response: `200 OK` with a list of notes.

### Level 2: LLM 'Rewrite This Entry'

**Generate alternative title:**
```bash
curl -u admin:admin -X POST http://localhost:8083/rest/entry/123/generate-alternative \
  -H "Content-Type: application/json" \
  -d '{"target": "title", "prompt": "Rewrite this headline to be more professional"}'
```
Response: `200 OK` with JSON:
```json
{
  "original": {
    "id": "123",
    "title": "Old Title",
    "content": "..."
  },
  "target": "title",
  "prompt": "Rewrite this headline to be more professional",
  "alternative": "New Professional Title"
}
```

## Translation

Files for internationalization are
located [here](https://github.com/Athou/commafeed/tree/master/commafeed-client/src/locales).

To add a new language:

- add the new locale to the `locales` array in:
    - `commafeed-client/.linguirc`
    - `commafeed-client/src/i18n.ts`
- run `npm run i18n:extract`
- add translations to the newly created `commafeed-client/src/locales/[locale]/messages.po` file

The name of the locale should be the
two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).

## Local development

### Backend

- Open `commafeed-server` in your preferred Java IDE.
    - CommaFeed uses Lombok, you need the Lombok plugin for your IDE.
- run `./mvnw quarkus:dev`

### Frontend

- Open `commafeed-client` in your preferred JavaScript IDE.
- run `npm install`
- run `npm run dev`

The frontend server is now running at http://localhost:8082 and is proxying REST requests to the backend running on
port 8083
<img width="1838" height="499" alt="image" src="https://github.com/user-attachments/assets/1e336aa4-4318-43ac-8bda-74ef526d9727" />



# jMolecules Java + ByteBuddy Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete the Java-first jMolecules migration by removing remaining Kotlin main sources, finalizing UUID-backed typed identifiers across `template` and `room`, rewriting the schema to UUID keys, and enabling `jmolecules-bytebuddy` only after the non-augmented model is stable.

**Architecture:** Treat `template` as the first fully finished UUID/jMolecules module, then use the same identity and persistence rules to cut over `room`. Keep module-root published surface minimal, separate `RoomCode` from `RoomId`, and delay ByteBuddy until the plain Java model, JPA mapping, and Liquibase schema all work without augmentation.

**Tech Stack:** Java, Kotlin tests, Spring Boot, Spring Modulith, jMolecules, Hibernate/JPA, Liquibase, PostgreSQL, Gradle, ByteBuddy

---

### Phase 1: Foundation / Template Typed-Id Support

Scope:

- switch `build.gradle.kts` from `kmolecules-ddd` to `jmolecules-ddd`
- remove `TemplateId` long bridge APIs
- add explicit typed-id conversion support for `template`
- ensure `TemplateCatalog` contract bean is available
- keep room create API/template-id transport on UUID string input

Checkpoint:

- structural tests pass
- targeted room API/create tests pass

### Phase 2: Room Domain Cutover

Scope:

- finish `RoomId` / `RoomCode`
- finish child UUID ids
- port `Room.kt` to `Room.java`
- update room child entity APIs consistently
- migrate Kotlin room tests away from Kotlin-only constructor/copy assumptions

Checkpoint:

- room domain and room-facing targeted tests pass
- no temporary public `long` bridge remains on room root identity surface

### Phase 3: Remaining Room Main-Source Java Port

Scope:

- port room application
- port room repository
- port room web
- port room exception

Checkpoint:

- `src/main/kotlin/com/naminhyeok/fantazzk/room/**` is removed
- room service/controller/repository tests pass

### Phase 4: UUID Schema Rewrite

Scope:

- rewrite Liquibase to UUID PK/FK across `template` and `room`
- update repository/module integration fixtures accordingly

Checkpoint:

- repository integration tests pass
- module integration tests pass

### Phase 5: ByteBuddy

Scope:

- wire `jmolecules-bytebuddy`
- remove only clearly redundant boilerplate

Checkpoint:

- full `test`, `integrationTest`, and `check` verification pass

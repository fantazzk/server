# Big-Bang Java Modulith + jMolecules Rewrite Design

## Goal

Rebuild the current Kotlin-based server as a Java-first Spring Modulith that follows Oliver Drotbohm's modulith philosophy closely, adopts jMolecules aggressively, enables `jmolecules-bytebuddy` from the first completed baseline, and standardizes all entity identifiers on UUID-backed typed identifiers.

This is a clean rewrite. Backward compatibility with the current code, schema, test suite, HTTP identifier formats, or migration history is explicitly out of scope.

## Why A Big Bang Rewrite

The current repository is still at an early stage. Supabase and Railway setup exist, but there is no real legacy estate that must be preserved. The existing migration attempt in PR `#37` already shows the core problem with an incremental approach:

- Kotlin and Java interop create extra friction during aggregate ports
- typed identifier migration leaks temporary bridge APIs into the design
- partially rewritten tests consume large amounts of time without improving the target architecture
- schema compatibility work keeps the codebase in an intentionally temporary midpoint

Given that there is no compatibility requirement, the rewrite should optimize for final architectural honesty rather than transition safety.

## Architectural Principles

The rewrite follows these principles:

- One Spring Boot application
- One Gradle project
- One root entrypoint: `com.naminhyeok.fantazzk.FantazzkApplication`
- Application modules are package-based, closed by default
- The real application modules are `template` and `room`
- Module root packages define the published surface
- Subpackages are internal implementation details
- Required synchronous collaboration is expressed through explicit contract beans
- Events are introduced only when there is a real asynchronous consumer
- `aggregate = domain model = JPA entity`
- jMolecules types and annotations are used to make architectural intent visible in code
- `jmolecules-bytebuddy` is part of the first completed target, not a follow-up experiment

## Module Structure

### Root Application

The root package remains `com.naminhyeok.fantazzk`.

- `FantazzkApplication` is the single application entrypoint
- root-level configuration exists only where real bean assembly is required
- wrapper configurations that only import already-scannable beans are not introduced
- `AutoConfiguration.imports` registration is not used

### Template Module

The `template` module owns immutable blueprint creation and retrieval.

Internal package layout:

- `template`
- `template.domain`
- `template.application`
- `template.repository`
- `template.web`
- `template.exception`

Published root surface:

- `TemplateId`
- `TemplateCatalog`
- `TemplateBlueprint`
- `TemplateCatalogException`
- `TemplateMode`
- `TemplateDraftOrderStrategy`

Everything else stays internal.

### Room Module

The `room` module owns room lifecycle, participation, auction, and draft progression.

Internal package layout:

- `room`
- `room.domain`
- `room.application`
- `room.repository`
- `room.web`
- `room.exception`

Published root surface:

- `RoomId`
- `RoomCode`

No extra room contract is published until a real external module consumer exists.

## Domain Model

### Template

`Template` is an immutable blueprint aggregate.

- Aggregate root: `Template`
- Child entity: `TemplatePlayer`
- Identifiers: `TemplateId`, `TemplatePlayerId`
- All identifiers are UUID-backed typed identifiers
- Template instances are created once and never mutated
- If a functional change is required, a new template is created instead of editing the existing one

The aggregate is responsible for:

- validating blueprint configuration
- validating roster size and order
- exposing blueprint state for internal use and external contract mapping

`TemplateBlueprint` is the cross-module read contract. It is not the aggregate itself.

### Room

`Room` is the lifecycle aggregate that owns runtime team-building rules.

- Aggregate root: `Room`
- Child entities: `RoomPlayer`, `RoomTeamLeader`, `RoomTeamMember`, `RoomBid`
- Identifiers: `RoomId`, `RoomPlayerId`, `RoomTeamLeaderId`, `RoomTeamMemberId`, `RoomBidId`
- All identifiers are UUID-backed typed identifiers
- `RoomCode` is a separate value object and business key

`Room` is responsible for:

- room creation
- host and participant join rules
- start rules
- auction bidding and settlement
- draft pick progression and settlement
- room completion

External participant keys such as `hostId` and `teamLeaderId` remain business or external identifiers. They are not collapsed into entity identity.

## Module Collaboration

The only required cross-module collaboration in the initial design is room creation from a template blueprint.

The collaboration rule is:

1. `room` synchronously calls the `template` module root contract `TemplateCatalog`
2. `room` receives a `TemplateBlueprint`
3. `room` maps the blueprint into its own internal creation model
4. `room` snapshots the relevant blueprint data into room-owned state
5. after creation, `room` no longer depends on `template` for lifecycle decisions

This is intentionally not a live reference model.

### Snapshot Policy

The room aggregate stores a snapshot of the template-derived state required to run the game.

- mode
- team count
- team size
- budget
- draft order strategy
- players to be used for the room

`Room` may also retain `originTemplateId` for traceability and diagnostics, but that identifier is not used for later reloading or rule recalculation.

This preserves a clean distinction:

- required consistency at creation time: synchronous contract call
- independent lifecycle after creation: room-owned state and rules

## Persistence Strategy

This rewrite does not separate domain entities from persistence entities.

- aggregate roots are JPA entities
- child entities are JPA-managed members of the aggregate
- value objects are embedded or converted where appropriate
- repository abstractions remain inside each module
- no separate mapper layer or duplicate persistence model is introduced

This is a pragmatic DDD model, not a purity exercise. The model should remain readable as a domain model first, with JPA and ByteBuddy enabling execution rather than dictating the structure.

## jMolecules And ByteBuddy Strategy

The rewrite uses jMolecules aggressively across the main codebase.

Planned usage:

- jMolecules identifier types for typed ids
- jMolecules DDD types for aggregates and identifiers
- `@org.jmolecules.ddd.annotation.Service` for application services
- `@org.jmolecules.ddd.annotation.Repository` for repository abstractions
- package-level architectural annotations via `package-info.java` where they improve role visibility

`jmolecules-bytebuddy` is required in the first completed baseline.

That means the first implementation milestone must prove all of the following together:

- Gradle build works with the chosen Java toolchain
- Spring Boot startup works
- JPA mapping works
- UUID-backed typed identifiers work through persistence and HTTP boundaries
- jMolecules ByteBuddy transformations are active and compatible with the project setup

ByteBuddy is not treated as an optional cleanup pass after the design is complete. It is part of the design target itself.

## Identifier Policy

All entity identities are UUID-backed typed identifiers from the start.

Examples:

- `TemplateId`
- `TemplatePlayerId`
- `RoomId`
- `RoomPlayerId`
- `RoomTeamLeaderId`
- `RoomTeamMemberId`
- `RoomBidId`

Rules:

- no numeric bridge constructors
- no legacy `Long` compatibility helpers
- no fallback numeric HTTP input support
- no dual identifier mode during the rewrite

Business identifiers remain distinct:

- `RoomCode` is a business lookup key
- `hostId` and `teamLeaderId` are external participant keys

## Schema Strategy

The database model is rewritten from scratch through Liquibase.

Rules:

- root table primary keys use UUID
- child table primary keys use UUID
- foreign keys use UUID
- `room.code` is unique
- old BIGINT identity shape is discarded completely
- old changelog compatibility is discarded completely

The first schema should reflect the target domain honestly instead of preserving temporary transition history.

### Audit Columns

Audit columns should match aggregate behavior instead of being added mechanically.

Recommended policy:

- immutable `template` and `template_player`: `created_at` only
- mutable room-side tables: `created_at` and `updated_at`

This avoids pretending that immutable template state is regularly updated when the model is explicitly designed not to permit mutation.

## Language Strategy

This rewrite is repository-wide Java, not production-only Java.

Scope:

- `src/main` rewritten in Java
- `src/test` rewritten in Java
- no Kotlin production source remains
- no Kotlin test source remains

The reason is architectural consistency. Keeping Kotlin tests while moving production code to Java would preserve exactly the interop costs that created friction in the earlier incremental migration attempt.

## Testing Strategy

The test suite is rewritten to prove the new architecture, not to preserve old test shapes.

### Test Categories

Unit tests:

- aggregate invariants
- value object rules
- auction and draft policies
- identifier behavior

Structural tests:

- `ApplicationModules.verify()`
- published surface and dependency direction checks
- jMolecules stereotype and package-role checks
- module canvas and PlantUML generation tests

Module tests:

- `@ApplicationModuleTest` for module boundaries and collaboration
- room creation through `TemplateCatalog`
- verification that room-template interaction occurs through contract beans only

Integration tests:

- JPA persistence and reload behavior
- Liquibase schema bootstrap
- Spring Boot wiring
- minimal HTTP adapter verification

### Test Source Layout

The first rewrite keeps a single test source set.

- use `src/test/java`
- do not keep a separate `src/integrationTest` source set initially
- keep test organization logical inside the package tree instead of relying on Gradle source set separation

The reason is to minimize build and tooling complexity during the rewrite. If test runtime later becomes a real problem, slow infrastructure tests can be split into a dedicated source set after the baseline architecture is stable.

## Web Adapter Scope

Web is not the center of the rewrite, but a minimal HTTP surface is still included in the first completed baseline because it is the cheapest end-to-end proof that identifiers, converters, module boundaries, and use cases work together.

Initial minimal HTTP scope:

`template`

- create template
- list templates
- get template detail

`room`

- create room
- join room
- start room
- place bid
- make draft pick
- get room detail

Swagger customization, broader documentation concerns, and secondary web-layer polish are deferred.

## Execution Order

Externally, this remains a single big-bang rewrite branch and a single cutover.

Internally, work still follows a strict order to avoid chaos:

1. establish a minimal runnable Java skeleton with Spring Boot, Spring Modulith, jMolecules, ByteBuddy, UUID identifiers, and a passing structural test baseline
2. finish the `template` module completely as the reference pattern
3. build the `room` module using the locked template pattern and snapshot collaboration rule
4. add the minimal HTTP adapters
5. finalize Liquibase schema and persistence verification
6. finish the full structural, module, and end-to-end verification suite

This is not incremental migration. It is a staged rewrite inside one coherent big-bang branch.

## Non-Goals

The following are explicitly out of scope for the rewrite baseline:

- compatibility with old numeric ids
- compatibility with current Liquibase history
- preserving existing Kotlin tests
- preserving existing Kotlin production files
- introducing events where there is no real asynchronous consumer
- keeping transitional bridge APIs for identifiers
- introducing extra abstraction layers only to mimic hexagonal structure

## Done Criteria

The big-bang rewrite is complete only when all of the following are true:

- `template` and `room` are the clear production modules
- published surface is minimal and intentional
- all entity identifiers are UUID-backed typed identifiers
- aggregate roots and key supporting types are expressed through jMolecules
- `jmolecules-bytebuddy` is active in the real build
- Liquibase bootstraps a fresh UUID-based schema
- `template` is immutable
- `room` snapshots template state at creation time and does not recalculate from live template reads
- the entire production and test codebase is Java
- `ApplicationModules.verify()` passes
- module canvas and PlantUML generation tests pass
- module tests pass
- persistence and minimal HTTP integration tests pass
- `test` and `check` are green on a clean run

## Key Risks To Handle Early

The first implementation tasks must de-risk these items immediately:

- ByteBuddy compatibility with the chosen Gradle and Java setup
- UUID typed identifier persistence and conversion
- package-level architectural expression with jMolecules in a Java-only build
- clean module-root contracts for `template`

If those foundations are not proven first, later room and schema work will only amplify the uncertainty.

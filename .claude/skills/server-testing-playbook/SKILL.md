---
name: server-testing-playbook
description: Use when choosing, reviewing, refactoring, or deleting tests in this repository, especially to decide test layers, doubles, fixtures, or regression coverage.
user-invocable: false
---

# Server Testing Playbook

## Overview

Tests in this repository are executable docs that protect domain rules, module boundaries, external contracts, and regression risk. Prefer the cheapest test that can prove the behavior without freezing the current implementation.

Read `references/regression-and-layers.md` when you need concrete examples of test-layer choices or good and bad regression tests.

## When To Use

- writing a new test
- choosing between `test` and `integrationTest`
- deciding whether to keep or delete an existing test
- reviewing fixture design, test doubles, or regression coverage

## When Not To Use

- basic command lookup already covered by `AGENTS.md` or `CLAUDE.md`
- purely production modeling questions with no testing tradeoff

## Choose The Test Type

- Unit test: aggregates, value objects, domain policies, deterministic orchestration
- `@WebMvcTest`: controller contract, validation, serialization, status codes
- `@DataJpaTest`: JPA mapping, repository queries, persistence behavior that H2 can represent
- `@ApplicationModuleTest`: module interaction itself is the subject under test
- `integrationTest`: external boundaries or runtime combinations that unit tests cannot sufficiently prove

Use `integrationTest` for things like:

- Testcontainers-backed database behavior
- locking and transaction isolation
- vendor-specific SQL, schema, or migration behavior
- HTTP integration using MockServer, WireMock, or similar boundary tooling

## Test Doubles

- Default to `fake` and `stub`
- Use `spy` only when the side effect itself is part of the contract and state assertions are not enough
- Use Mockito sparingly
- Use `@MockitoBean` only in Spring slice tests when boundary control matters

Smells:

- multiple `@MockitoBean` declarations in one test class
- tests that mainly verify call order or call count
- thin orchestration tests that only prove delegation

## Determinism

- Domain logic should be testable without Spring
- Push time, randomness, and external state to boundaries
- Use `Clock` for time
- Do not wrap every technical UUID by default
- When an identifier or code has business meaning, use an explicit generator such as `RoomCodeGenerator`
- Do not call random APIs directly inside domain rules when the random outcome changes business behavior

## Fixtures

- Default to test data builders
- Allow small static helpers when they remove obvious noise
- Do not use large object mothers as the primary strategy
- Important scenario values must stay visible in the test body
- If a reader has to open multiple files to understand the setup, the fixture abstraction is too deep
- Do not distort production code just to make fixture setup easier

## Regression Tests

- A bug fix should usually add regression protection
- Regression protection can be a new test, a stronger existing test, or a stronger structural rule
- Protect the observable result or contract, not the current implementation shape
- Add the regression at the lowest-cost layer that can prove the behavior

Bad regression tests:

- freezing helper calls, call order, or method decomposition
- asserting implementation choreography instead of business outcome

## Thin Application Services

Keep explicit tests when the service owns meaningful policy, for example:

- branching rules
- transaction boundaries
- permission checks
- event publication
- idempotency, retry, or lock-sensitive orchestration

Treat tests as optional or removable when the service is only:

- `load -> invoke aggregate -> save`
- trivial delegation already covered by lower-cost tests

## Test Refactoring

- Test code is a maintained asset and can be refactored
- Fixture code is also subject to cleanup or deletion
- If harmless production refactors break many tests, the tests are probably too implementation-coupled
- Remove low-value tests that no longer protect real risk

## Verification Workflow

- During implementation: run targeted tests or `./gradlew test`
- Before concluding local work: run `./gradlew check`
- Before commit or review: run `./gradlew integrationTest`

`integrationTest` is excluded from the default inner loop because it lengthens feedback, not because it is optional.

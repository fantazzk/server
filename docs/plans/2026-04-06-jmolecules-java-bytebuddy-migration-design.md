# jMolecules Java + ByteBuddy Migration Design

## Goal

Rebuild `src/main` as a Java-first Spring Modulith that follows Oliver Drotbohm's jMolecules style closely, uses UUID-backed typed identifiers, minimizes module-root published surface, and becomes ready for `jmolecules-bytebuddy` augmentation only after the non-augmented model and persistence are stable.

## Core Intent

- `src/main` becomes Java-only
- `src/test` and `src/integrationTest` remain Kotlin
- `kmolecules-ddd` is replaced by `jmolecules-ddd`
- aggregate root ids become UUID-backed typed identifiers
- child entity ids also become UUID-backed typed identifiers
- `RoomCode` remains a separate business key value object
- module-root published surface stays minimal
- no public legacy-support API is desired in the final state
- `jmolecules-bytebuddy` is enabled only after plain Java model + schema stabilization

## Module Boundaries

Published root contracts:

`template`
- `TemplateId`
- `TemplateCatalog`
- `TemplateBlueprint`
- `TemplateCatalogException`
- `TemplateMode`
- `TemplateDraftOrderStrategy`

`room`
- `RoomId`
- `RoomCode`

Everything else should stay internal unless a real cross-module consumer proves otherwise.

## Identity Model

Aggregate roots:

- `TemplateId`
- `RoomId`

Child entities:

- `TemplatePlayerId`
- `RoomPlayerId`
- `RoomTeamLeaderId`
- `RoomTeamMemberId`
- `RoomBidId`

Business keys remain distinct from entity identity:

- `RoomCode` is the public room lookup key
- `teamLeaderId` remains the business/external participant key

## Persistence Direction

Target schema:

- `template` and `room` root tables use UUID primary keys
- child tables use UUID primary keys
- root foreign keys use UUID
- `room.code` remains unique as a business key

The migration does not need backward compatibility for:

- old HTTP id formats
- old `Long` constructor bridges
- current BIGINT identity schema

## Sequencing Principle

Internal sequencing still matters even if the user prefers large coherent batches:

1. stabilize Java/jMolecules foundation
2. finish `room` domain cutover in one coherent batch
3. port remaining `room` main sources to Java
4. rewrite Liquibase to UUID PK/FK schema
5. only then enable `jmolecules-bytebuddy`

## Non-Goals

- converting existing Kotlin tests to Java
- preserving current BIGINT schema shape
- preserving old numeric HTTP ids
- introducing ByteBuddy before plain Java model and schema stabilize

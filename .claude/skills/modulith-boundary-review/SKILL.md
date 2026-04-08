---
name: modulith-boundary-review
description: Use when reviewing or changing Spring Modulith package boundaries, cross-module contracts, events, or module tests in this repository.
user-invocable: false
paths:
  - src/main/java/**
  - src/test/java/**/architecture/**
---

# Modulith Boundary Review

## Overview

Spring Modulith boundaries should make dependency direction and collaboration style obvious. Keep module contracts narrow, prefer the module root as the primary home for core types, keep subpackages internal, and use synchronous contracts and asynchronous events for different reasons rather than interchangeably.

Read `references/boundary-smells.md` when you need concrete examples of overexposed module APIs, RPC-by-event, or coupling-heavy module tests.

## When To Use

- creating or splitting a module
- exposing a new cross-module contract
- deciding between direct contract and domain event
- reviewing a package structure or module test for coupling problems

## When Not To Use

- local refactors that stay fully inside one module and do not touch published contracts
- domain invariant questions better handled by `domain-modeling-playbook`

## Boundary Rules

- Application modules are defined by package roots
- Only public types in a module root are cross-module contracts
- Subpackages are internal implementation details
- Keep public surface minimal
- Do not depend directly on another module’s internal types, repositories, queries, or application internals

## Direct Contract vs Event

- Use an explicit contract bean when the collaboration is synchronous and required for consistency
- Use a domain event when the collaboration is a follow-up reaction and can be decoupled
- Do not use events as an RPC replacement
- Do not add an event if there is no real consumer

## Packaging And Coupling

- Do not default every module to `application`, `api`, `domain`, `repository`, `query`, and `infrastructure`
- Prefer the module root for core domain and orchestration types
- Add subpackages only for secondary concerns with clear value, such as `web`, `config`, `internal`, or intentionally exposed named interfaces
- Use `application`, `api`, `repository`, `query`, and `infrastructure` only when the role is clear and the extra package boundary pays for itself
- Do not create interface/impl pairs with no boundary value
- Avoid `spi` as a default pattern
- If a module test needs many mocked neighboring modules, treat that as a coupling smell

## Query And Read Model Guidance

- CQRS is a tool, not a goal
- Keep `query` packages only when they add real decoupling or API value
- Do not build read models that merely wrap the write model again
- Keep read-side details internal unless they are a true external contract

## What To Test

- `ApplicationModules.verify()` should always pass
- Use `@ApplicationModuleTest` only when module interaction itself matters
- Strengthen structural tests before adding cross-module behavior tests that freeze implementation details

## Review Questions

- Is this type really part of the module’s public contract?
- Is this collaboration required now, or is it a later reaction?
- Would another module still work if this internal package moved tomorrow?
- Is this event carrying a real decoupled reaction, or hiding a synchronous dependency?
- Did this test prove a boundary, or just preserve current wiring?

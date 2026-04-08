---
name: domain-modeling-playbook
description: Use when introducing or refactoring aggregates, value objects, domain policies, or application-service/domain boundaries in this repository.
user-invocable: false
paths:
  - src/main/java/**
---

# Domain Modeling Playbook

## Overview

Model the business language and consistency rules first. Keep infrastructure at the edge, keep the domain deterministic, and let aggregates own invariants instead of turning application services into decision engines.

Read `references/modeling-smells.md` when you need concrete examples of anemic models, oversized aggregates, or misplaced orchestration.

## When To Use

- introducing a new aggregate or major domain type
- deciding whether something is an entity, value object, service, or policy
- moving business rules between aggregate and application layers
- reviewing whether a model is becoming an anemic data bag

## When Not To Use

- mechanical controller, DTO, or repository wiring with no domain decision
- pure test-layer questions better handled by `server-testing-playbook`

## Modeling Flow

1. Name the rule or invariant in domain language
2. Decide what must stay consistent together
3. Choose the smallest aggregate that can protect that consistency boundary
4. Keep orchestration outside the aggregate and rules inside it
5. Push time, randomness, and external state to explicit boundaries

## Entities, Value Objects, Aggregates

- Use a value object when identity does not matter and equality is by value
- Use an entity when continuity and identity matter over time
- Use an aggregate only when you need a consistency boundary with behavior and invariants
- Prefer references by identifier across aggregate boundaries instead of rich object graphs

## Application Services

- Default flow: `load -> invoke aggregate -> save`
- Keep application services thin when they only orchestrate
- Put branching business rules on the aggregate or a domain policy when they belong to the domain model
- Do not let application services become decision dumps that move all invariants out of the model

## Deterministic Design

- Domain behavior should be testable without Spring
- Use `Clock` for time
- Do not wrap every technical UUID by default
- Use explicit generators only when the produced identifier or code has business meaning
- Do not call random APIs directly inside domain rules when the random outcome changes behavior

## Modeling Smells

- setters that allow invalid intermediate states
- aggregate methods that only expose data and leave all decisions to services
- application services that contain all the real rules
- public methods or hooks added only for tests or persistence convenience
- aggregates that are too large because unrelated consistency rules were grouped together

## Review Questions

- What invariant is this model protecting?
- Does this type need identity, or is it really a value object?
- Is this aggregate the smallest boundary that can keep the rule true?
- Is the application service orchestrating, or secretly owning domain policy?
- Would this still be easy to test if Spring disappeared from the picture?

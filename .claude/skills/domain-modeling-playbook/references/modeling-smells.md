# Modeling Smells

## Likely Anemic Model

- aggregate methods only expose data
- application service contains all branching and rule decisions
- invariants are checked only in controllers or services

## Aggregate Too Large

- unrelated rules change together only because data lives in one object
- many commands touch disjoint parts of the aggregate without shared consistency needs
- tests require huge fixtures to exercise small pieces of behavior

## Boundary Leakage

- domain methods depend directly on Spring or persistence infrastructure
- time, randomness, or external state is pulled from globals inside domain code
- production API surface grows only to make tests easier

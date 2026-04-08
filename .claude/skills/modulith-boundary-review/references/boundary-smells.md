# Boundary Smells

## Overexposed Contract

- public types exist in a module root without real cross-module value
- another module imports subpackage implementation details
- query or repository types are used as ad-hoc cross-module APIs

## RPC By Event

- an event exists only because a synchronous dependency was avoided cosmetically
- a producer assumes exactly one consumer must react immediately
- event payload is too thin, forcing consumers to reload write-side state

## Coupling-Heavy Tests

- `@ApplicationModuleTest` needs many mocked neighboring modules
- the test mainly preserves current wiring rather than a module contract
- structural rules could prove the boundary more cheaply than behavior tests

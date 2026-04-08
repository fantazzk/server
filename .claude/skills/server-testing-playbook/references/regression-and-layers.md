# Regression And Layer Examples

## Layer Choice

- Use a unit test when an aggregate or policy can prove the rule without Spring.
- Use `@WebMvcTest` when the contract under review is request mapping, validation, serialization, or status code behavior.
- Use `@DataJpaTest` when H2 is enough to validate mapping and repository behavior.
- Escalate to `integrationTest` when you need real database semantics, external HTTP boundaries, or realistic runtime composition.

## Good Regression Tests

- A tie-break rule test that proves the winner selection result
- A controller test that proves an invalid request returns the expected status and payload
- A persistence test that proves a locking or isolation rule with the real database

## Bad Regression Tests

- Verifying helper call order after a bug fix
- Freezing private method decomposition
- Adding a broad end-to-end test when a focused lower-cost test could prove the same rule

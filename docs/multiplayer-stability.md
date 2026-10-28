# Multiplayer and stability review

Milestone 17 reviewed the common/server boundary and the state flow used by CFR
signals.

## Verified in code

- Client renderer, model registration, screen, and client payload handling are
  confined to `ro.cfrsignals.client` and the client-only mod entry point.
- Create graph and `SignalBoundary` access occurs only during logical-server
  block-entity ticks.
- The server validates configuration enum IDs, plate text, build permission,
  interaction permission, wrench possession, distance, loaded chunk, and block
  entity type before applying a client packet.
- Configuration and aspects persist through block-entity NBT and synchronize
  only on meaningful changes.
- Semaphore motion is interpolated locally; no per-frame packets are sent.
- Following-signal topology is cached and refreshed instead of traversing the
  graph every tick.
- Breaking the controller delegates cleanup to Create's `IBE` and targeting
  behavior lifecycle.

## Fail-safe recovery

A controller that has never been track-targeted retains manual debug control.
If a configured target track is removed or unavailable, the controller now
reports `TRACK_MISSING` and displays RED. It resumes Create control when the
target becomes valid again. This prevents a previously green indication from
remaining green after its protected track link disappears.

## Runtime coverage and remaining manual tests

Automated tests cover configuration persistence and sanitation, packet value
validation, Create-state mapping, advance-aspect decisions, semaphore motion,
definition validation/fallbacks, and missing-track fail-safe resolution.

Dedicated-server startup verifies classloading and data reloads. True
multi-client concurrency, chunk unload/reload around targeted track, graph
rebuilds during train movement, reconnects, and restart persistence still
require manual testing with two game clients and a representative Create rail
layout.

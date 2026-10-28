# Create 1.21.1 signalling API inspection

This document records the Create classes inspected for CFR Signals Milestone 2. It is intentionally tied to the exact dependency version below; classes under Create's `content` packages are public Java types, but they are not a separately versioned stable API.

## Verified dependency set

- Minecraft: `1.21.1`
- NeoForge: `21.1.250`
- Create development artifact: `com.simibubi.create:create-1.21.1:6.0.10-280:slim`
- Create runtime/mod version: `6.0.10`
- Create Git commit: `ac0c444d9828da3453ae8cc65338e8de063286fb`
- Ponder: `1.0.82+mc1.21.1`
- Flywheel: `1.0.6`
- Registrate: `MC1.21-1.3.0+67`

The Create JAR manifest and the official `mc1.21.1-6.0.10` source tag report the same commit. The local NeoForge version is newer than Create's declared minimum of `21.1.219`.

## Relevant classes

### `SignalBlock`

`com.simibubi.create.content.trains.signal.SignalBlock`

- Create's physical signal block.
- Implements Create's `IBE<SignalBlockEntity>` and `IWrenchable` helpers.
- Stores `ENTRY_SIGNAL` or `CROSS_SIGNAL` plus redstone power as block-state properties.
- Is not a suitable superclass for the CFR controller because its state, block entity type, and wrench behavior are specifically tied to Create's own signal block.

### `SignalBlockEntity`

`com.simibubi.create.content.trains.signal.SignalBlockEntity`

- Extends Create's `SmartBlockEntity`.
- Owns a public `TrackTargetingBehaviour<SignalBoundary>` named `edgePoint`.
- Registers that behaviour with `EdgePointType.SIGNAL`.
- On the server, reads `SignalBoundary.getStateFor(worldPosition)` and synchronizes changes through `notifyUpdate()`.
- Exposes the public nested state enum `RED`, `YELLOW`, `GREEN`, and `INVALID` through `getState()`.
- Its actual `state` field is private, so subclassing it would couple CFR Signals to Create's block entity and visual behavior without providing useful control. CFR Signals should use its own block entity and compose with the targeting behaviour instead.

### `TrackTargetingBehaviour<T>`

`com.simibubi.create.content.trains.track.TrackTargetingBehaviour`

This is the preferred integration seam.

- Public constructor accepts a `SmartBlockEntity` and an `EdgePointType<T>`.
- Persists the target track offset, direction, curve segment, edge-point UUID, and migration state in the owning block entity's behaviour NBT.
- Resolves an existing edge point by UUID from the appropriate railway manager.
- Creates and registers a new edge point server-side when a valid target exists.
- Merges compatible signal block entities onto an existing `SignalBoundary`.
- Calls `blockEntityRemoved` when the owning block entity is destroyed, allowing Create to remove an unused boundary.
- Exposes `getEdgePoint()`, `hasValidTrack()`, `getGlobalPosition()`, `getTargetDirection()`, `getTargetBezier()`, and `determineGraphLocation()`.
- Its static overlay renderer is client-only. Common CFR code must not call that method or import its rendering parameter types.

The owning CFR block entity will therefore need to extend `SmartBlockEntity`, add a `TrackTargetingBehaviour<SignalBoundary>` using `EdgePointType.SIGNAL`, and keep renderer registration in the client package.

### `TrackTargetingBlockItem`

`com.simibubi.create.content.trains.track.TrackTargetingBlockItem`

- Implements Create's two-step track selection and block placement workflow.
- Validates normal and curved track locations with the railway graph.
- Rejects junctions, invalid tracks, and incompatible nearby edge points.
- Stores the selected position, direction, and curve segment in Create data components until placement.
- Writes the target data into the placed block entity and clears the temporary item state.
- Requires the placed block to implement Create's `IBE<?>`, because it queries the block entity type during placement.
- The public `ofType(EdgePointType.SIGNAL)` factory can be used for a CFR signal block item after the controller block and block entity exist.

This is preferable to recreating Create's track picker or searching for trains.

### `SignalBoundary`

`com.simibubi.create.content.trains.signal.SignalBoundary`

- The actual signal edge point stored in a `TrackGraph` under `EdgePointType.SIGNAL`.
- Can associate block entities on either side of one railway boundary.
- `getStateFor(BlockPos)` publicly returns the cached Create signal state for an associated block entity.
- `getOverlayFor(BlockPos)` exposes whether a single or dual track overlay is appropriate.
- `getTypeFor(BlockPos)` reports entry versus cross-signal behavior.
- `isForcedRed(...)` and group identifiers are public.
- State calculation itself is private and occurs during Create railway ticks after train occupancy has been resolved.

`blockEntityAdded` accepts any block entity. A non-Create CFR block entity is registered normally; it simply does not participate in Create's own redstone-power override. If the CFR block lacks Create's `SignalBlock.TYPE` property, the new boundary retains its default `ENTRY_SIGNAL` type. That gives the required initial red/green behavior.

### `SignalEdgeGroup`

`com.simibubi.create.content.trains.signal.SignalEdgeGroup`

- Represents a protected signal section and its intersecting groups.
- Tracks occupying trains and an optional reservation.
- Public `isOccupiedUnless(SignalBoundary)` and `isOccupiedUnless(Train)` methods expose occupancy checks.
- CFR's first integration should consume the boundary's cached state instead of reimplementing this calculation.

### `EdgePointType`, `TrackGraph`, and `EdgeData`

- `EdgePointType.SIGNAL` is the registered public edge-point type for `SignalBoundary`.
- `TrackGraph.getPoint`, `getPoints`, `addPoint`, and `removePoint` are public.
- `EdgeData.next(EdgePointType, position)` can locate a following edge point on one edge.
- These types make later graph inspection possible, but routine CFR updates should retain the resolved boundary from `TrackTargetingBehaviour` rather than scanning every graph or signal collection.

### `GlobalRailwayManager`

`com.simibubi.create.content.trains.GlobalRailwayManager`

- Available as `Create.RAILWAYS` on the server.
- Owns public maps for track networks, signal edge groups, and trains.
- Its `sided(LevelAccessor)` method selects the server or client railway manager.
- Railway ticking clears/rebuilds occupancy, ticks edge points before trains, ticks trains, then ticks edge points again to calculate cached signal states.

Direct global-map access should be limited to diagnostics and recovery. Normal controller updates can use the targeting behaviour's resolved edge point.

## State flow to reuse

1. `TrackTargetingBlockItem` selects a track graph location and places the controller with target data.
2. `TrackTargetingBehaviour` restores or creates an `EdgePointType.SIGNAL` point.
3. The graph owns a `SignalBoundary` and Create's railway manager updates its section groups and occupancy.
4. `SignalBoundary` caches the aspect for each associated block position.
5. The CFR controller reads `boundary.getStateFor(worldPosition)` on the logical server.
6. A CFR aspect resolver maps Create's state into the internal `CFRSignalAspect` model.
7. The block entity synchronizes only when that mapped aspect changes.
8. Client rendering consumes the synchronized CFR aspect without querying the railway graph.

This preserves Create as the authority for railway occupancy and avoids arbitrary train searches.

## Yellow behavior found in Create

Create already has a yellow state for `CROSS_SIGNAL` boundaries. Its private chain resolver returns:

- green when all reachable chained paths are free,
- red when no chained path is free,
- yellow when only some chained paths are free,
- invalid when the chain data cannot be resolved.

This is useful evidence for Milestone 10, but it is not yet assumed to be identical to the requested CFR advance-signal rule. Following-section semantics must be studied separately before automatic CFR yellow is implemented.

## Rendering findings

- `SignalRenderer` is Create's non-Flywheel fallback block entity renderer.
- `SignalVisual` is Create's Flywheel visual.
- Both are client-only in practice, are typed specifically to `SignalBlockEntity`, and use Create's own hard-coded partial models.
- They should not be subclassed or copied for CFR geometry.
- CFR Signals should register its own renderer and later its own Flywheel visual, while reusing the targeting and boundary state on the common/server side.

## Access and stability constraints

- No mixin, access transformer, or reflection is required for the proposed red/green integration.
- The required constructors and state getters are public in the resolved slim JAR, confirmed with `javap`.
- Most useful types live under `com.simibubi.create.content`, not `com.simibubi.create.api`; they are accessible but may change between Create releases.
- Create versions must stay pinned and these integration points must be rechecked before upgrades.
- `SignalBoundary.tickState` and `resolveSignalChain` are private. CFR Signals should consume their public result, not try to invoke or replace them.
- Common code must avoid references to `SignalRenderer`, `SignalVisual`, `TrackTargetingClient`, and the client-only static render method on `TrackTargetingBehaviour`.

## Recommended later implementation

Milestone 3 should remain Create-independent in behavior even though the dependency is present. In Milestone 6, the CFR controller can adopt Create's `SmartBlockEntity`/`IBE` composition and `TrackTargetingBlockItem` workflow. Milestone 7 can then map Create `RED` and `GREEN` from `SignalBoundary.getStateFor` into CFR aspects without graph scans or invasive patches.

## Milestone 6 implementation

The controller now uses the inspected public integration seam directly:

- Its registered item is a `TrackTargetingBlockItem` configured with `EdgePointType.SIGNAL`.
- `CFRSignalBlockEntity` owns a `TrackTargetingBehaviour<SignalBoundary>` and registers it through `SmartBlockEntity.addBehaviours`.
- The item writes Create's `TargetTrack`, `TargetDirection`, optional `Bezier`, and block-entity type data during placement.
- The behaviour persists its edge-point UUID and target data, resolves or creates the boundary, merges with compatible points, and unregisters the controller when destroyed.
- Sneak-use with an empty hand reports the target track position, direction, straight/curve selection, and resolved `SignalBoundary` UUID.

Milestone 6 deliberately does not call `SignalBoundary.getStateFor`. The CFR aspect remains under the existing debug control until Milestone 7.

## Milestone 7 implementation

The CFR controller now follows the same public state-consumption path as Create's own signal block entity:

- After `SmartBlockEntity.tick()` has updated its behaviours, the logical server resolves the linked `SignalBoundary` and calls `getStateFor(worldPosition)`.
- Create `GREEN` maps to the CFR two-light `GREEN` aspect.
- Create `RED`, `YELLOW`, and `INVALID` map to the fail-safe CFR `RED` aspect. Automatic yellow remains reserved for the separately researched advance-signal logic in Milestone 10.
- `setAspect` ignores unchanged values, so block-entity persistence and `notifyUpdate()` only occur for visible aspect transitions.
- Client rendering continues to consume only the synchronized CFR aspect and never queries Create's railway graph.
- An entirely unlinked controller retains its manual RED/GREEN debug interaction. Once a track target exists, Create is authoritative and manual aspect changes are disabled.
- Sneak-use diagnostics report both the raw Create state and mapped CFR aspect.

## Milestone 10 advance-signal implementation

Inspection of Create 6.0.10 confirmed a public graph traversal seam for the next protected sections:

- `SignalPropagator.collectChainedSignals(TrackGraph, SignalBoundary, boolean)` walks outward from one side of the current boundary and returns the next `SignalBoundary` UUID and approached side on every reachable branch.
- The traversal stops at the first boundary on each branch, so CFR Signals does not scan global train collections or guess from block distance.
- The returned side selects `SignalBoundary.cachedStates`; sides without associated signal block entities are ignored, matching Create's own signal-chain resolver.
- The controller obtains its graph through `TrackTargetingBehaviour.determineGraphLocation()` and validates cached graph ownership with `TrackGraph.getPoint`.

The controller caches this following-boundary topology per block entity. It rebuilds the cache when the graph, boundary, side, or railway-manager version changes, with a staggered 100-tick fallback refresh for topology changes that do not advance that version. Cached following states are polled every server tick, so train occupancy changes update the displayed aspect without repeating the graph walk.

For a three-light signal, a restrictive current Create state remains CFR red. A clear current state becomes CFR yellow if any reachable following boundary is red, yellow, invalid, or temporarily missing; it becomes green only when every active following side is green, or when no following signal exists. This deliberately conservative, route-agnostic junction policy prevents a clear indication while any reachable branch is known to be restrictive. Two-light signals continue to use only the current section.

All graph access remains on the logical server. The client renderer consumes only the synchronized CFR aspect.

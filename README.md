# CFR Signals

CFR Signals is a NeoForge mod for Minecraft 1.21.1 that will add Romanian railway signals integrated with Create's railway signalling system.

The project is currently at Milestone 13: the shared CFR signal controller supports modular two-light and three-light heads plus a polished CFR mechanical semaphore. Every default signal stands roughly six blocks tall. The mechanical semaphore also has a long white-striped arm with a stepped round end, a dark top mechanism, compact red/green lamps, and smoothly interpolated arm, counterweight, and linkage motion. Create remains authoritative for railway state, and each target transition produces one local mechanical clunk without animation packets.

Milestone 14 adds validated data-driven visual definitions under `data/<namespace>/cfr_signal_definitions`. These describe families, model identifiers, lamp arrangements, supported attachments, and defaults while leaving Create railway behavior and server authority in Java. See `docs/signal-definitions.md` for the schema and fallback behavior.

Milestone 15 adds initial survival recipes for the signal components. Signal configuration now uses Create's native wrench; the former CFR wrench remains registered only to preserve old-world inventories. Recipe details are documented in `docs/survival-recipes.md`.

Milestone 16 verified the bundled Ponder 1.0.82 API. Scene registration is intentionally deferred until CFR structure templates can be authored after the remaining visual passes; the exact API findings and required templates are recorded in `docs/ponder-integration.md`.

Milestone 17 adds fail-safe handling for removed or unavailable targeted track: linked signals now display RED and expose a `TRACK_MISSING` diagnostic until Create's target becomes valid again. The multiplayer/server review is recorded in `docs/multiplayer-stability.md`.

Milestone 18 adds persisted LEFT/CENTER/RIGHT head alignment. Modern signals now default to CENTER, side-mounted heads receive a visible mirrored bracket, and all placement offsets are centralized in `CFRSignalVisualLayout` without changing Create railway links.

Milestone 19 centers every lens at the housing's model center, reduces lens diameter, and replaces the prototype square bezel plates with open dark rings beneath the existing deep visors.

Milestone 20 gives the modern families separate CFR proportions: the three-light housing is taller with evenly spaced lamps, the mast/ladder/cabinet are slimmer, and the default assembly is roughly six blocks high after the requested two-block reduction. Exact scale choices are recorded in `docs/cfr-visual-proportions.md`.

Milestone 21 adds a real rear casing, continuous mounting spine, edge hardware, and stepped visor shoulders to both modern heads. The result has a readable front, side, and rear profile without runtime-generated or high-polygon geometry.

The modern head assembly is rendered at 1.2 scale and shifted forward by 3.5 model units. This makes the housing readable at normal gameplay distance and prevents it from occupying the pole volume; only the mounting spine and brackets meet the mast.

Milestone 22 gives the mechanical family one additional mast segment, scales its complete mechanism to 1.15, and extends the arm to a 2.3-block effective reach from the pivot. Dedicated CFR red, warm off-white, dark-steel, and blackened-housing textures keep the semaphore weathered but maintained without changing modern signal materials.

Milestones 23 and 24 finish the identification plate's visible face and dynamic lettering. The plate now has a framed warm off-white panel, dark centered text, and automatic scale-down for values up to the validated eight-character limit. See `docs/identification-plates.md`.

Milestone 25 adds validated HIGH/NORMAL/LOW plate positions. The configuration UI sends only the enum selection, and existing signals migrate to NORMAL.

Milestone 26 adds visible center-head saddles, cabinet clamp bands, and a mechanical housing saddle/gusset. The mounting audit is documented in `docs/component-mounting.md`.

Milestone 27 polishes the configuration screen with clear sections, contextual tooltips, persistent draft values across GUI rebuilds, keyboard submission, and family-aware controls. Mechanical semaphores disable the irrelevant modern-head alignment selector without altering saved server state.

The pre-Milestone-28 visual correction expands the block-entity render bounds for the complete rotating semaphore, enlarges the identification plate and separates its text from the face, and binds semaphore materials to the proven shared CFR block atlas paths to eliminate magenta fallback surfaces.

Milestone 28 completes a focused visual-performance pass. Static parts remain baked models in one block entity, all solid/cutout geometry shares one cached render type, and dynamic identification text is omitted beyond 48 blocks where it is no longer readable. See `docs/visual-performance.md` for the audit and large-yard test procedure.

Milestone 29 adds repeatable final visual-integrity tests for every CFR block model and texture. The suite rejects missing texture references, undecodable or non-power-of-two textures, fallback-magenta pixels, invalid model bounds, zero-size elements, directional cull faces, semaphore arm gaps, and detached or z-fighting identification plate placement. The remaining camera-angle and weather matrix is documented in `docs/final-visual-validation.md` for manual in-game confirmation.

Optional Milestone 30 adds final player-facing polish: localized controller/Create-wrench guidance and complete Romanian (`ro_ro`) language coverage with automated English-key parity. Existing local semaphore clunks and safe centered modern defaults are retained; unstable Ponder scenes and placeholder custom sounds are deliberately not added.

The physical-assembly revamp replaces the projected one-block structure with
three real block types. Build upward as `Signal Base -> one or more Signal Pole
blocks -> Signal Head`. The base includes the mandatory electrical cabinet and
each alternating red/white pole section includes its mandatory ladder. The head
owns the Create link, aspect, family, optional indicator, and identification
plate. Missing, over-height, or differently oriented supports force the head to
red and produce an in-game diagnostic. See `docs/signal-option-compatibility.md`.
Compatible electric heads may instead use the wall variant, which requires a
solid block directly behind the head and renders dedicated supports for both
the signal housing and identification plate.

Signal bridges use the same physical system. Put a bridge-beam block on top of
an aligned pole, then continue the beam sideways. Extend it on one side for an
L-shaped cantilever or on both sides for a T-shaped bridge. Place luminous
signal heads directly below a beam segment and mechanical semaphores directly
above one, then select **Signal bridge / cantilever** with Create's wrench. Mechanical
semaphores use a pedestal so the mechanism and moving arm remain above the
crossbeam. A head accepts a supporting pole and base up to eight connected
beam segments away, and every head keeps its own Create track link.

For an inline head partway up a mast, build to the desired height, place the
signal head directly on top of the pole, and select **Inline continuous pole**
with Create's wrench. Then continue placing pole blocks directly above the head. The
head renders the missing mast-and-ladder section behind its housing, creating an
unbroken column. More inline heads can be inserted higher on the same mast, and
each remains an independent Create-linked signal. The complete column must stay
aligned and terminate at a CFR signal base below.

Inline mechanical semaphores project their complete mechanism forward from the
continuous pole. Only the housing's rear mounting edge overlaps the mast, so the
pole remains visually continuous without passing through the mechanism.

## Development requirements

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.250
- Create 6.0.10

Build on Windows with:

```powershell
.\gradlew.bat build
```

The inspected Create railway integration points are documented in [`docs/create-signal-api-1.21.1.md`](docs/create-signal-api-1.21.1.md).

The model coordinate conventions, module boundaries, and Blockbench import/export procedure are documented in [`docs/blockbench-workflow.md`](docs/blockbench-workflow.md).

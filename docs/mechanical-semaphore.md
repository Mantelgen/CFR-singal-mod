# Mechanical semaphore rendering

Milestones 12 and 13 add the first mechanical family to the existing controller rather than introducing a second block entity or railway integration.

## State and animation flow

1. The server reads the linked Create `SignalBoundary` through the existing resolver.
2. A restrictive current section produces CFR `RED`; a clear current section produces CFR `GREEN`.
3. The existing block-entity update packet synchronizes only the changed aspect/configuration.
4. On the client, `CFRSignalBlockEntity` derives a target arm angle from the synchronized aspect.
5. Client ticks approach that target by 2.5 degrees per tick, while the renderer interpolates between ticks.

The stop target is 0 degrees and proceed is 45 degrees. The counterweight shares the arm shaft rotation, while the linkage moves in the opposite direction at 35 percent of the arm travel. No packet contains a mechanical angle, and the server does not update animation frames.

When the synchronized target changes, each client plays one quiet local iron mechanism sound. Initial chunk loading is silent, and subsequent animation frames do not replay the sound.

## Modular models

The renderer assembles three standalone models under `models/block/mechanical`:

- `semaphore_arm.json` — long CFR-red arm with a continuous white stripe and stepped round end;
- `semaphore_pivot.json` — axle and pivot cap;
- `semaphore_linkage.json` — moving vertical linkage;
- `semaphore_counterweight.json` — weighted lever rotating on the arm shaft.
- `semaphore_mechanism_housing.json` — dark machinery enclosure and raised cap behind the pivot.

Modern signals render with a minimum five-block mast. The mechanical family adds one mast segment and therefore uses a minimum six-block mast, placing its machinery approximately one block above the modern heads. The mechanical identification plate remains below the raised mechanism, while the ladder follows the full mast. A compact scaled two-light module sits below the arm: RED illuminates its upper lamp and GREEN its lower lamp, matching the arm aspect. This visual height is handled by the controller renderer and render bounding box; it does not create another block entity.

The complete mechanical assembly is rendered at 1.15 scale around its fixed shaft. The extended arm has an effective reach of approximately 2.3 blocks from that pivot. Its red body carries a continuous white stripe, and the outer end uses overlapping stepped cuboids to form a large pixel-rounded red disc with a white inset on both faces.

Mechanical mast bands, ladder, arm, pivot, linkage, counterweight, and housing use dedicated 16x16 material textures. The source material atlas is retained at `docs/assets/m22-mechanical-texture-source.png`; its four quadrants were reduced to CFR red paint, warm dirty white paint, dark oiled metal, and blackened housing metal. The textures use restrained wear so the equipment looks maintained rather than abandoned.

The models use the same block-model and Blockbench-compatible workflow as the modern heads. The arm model is authored in its horizontal stop pose around the pivot at `(8, 12, 8)` model units. Runtime rotation occurs around that point. Left mounting rotates the assembly around the mast while preserving upward proceed movement.

The sound currently reuses Minecraft's iron-trapdoor open/close events as a placeholder until dedicated CFR audio assets are available.

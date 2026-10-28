# Final CFR visual validation

Milestone 29 combines automated asset validation with a manual in-game inspection matrix. Automated success does not substitute for inspecting actual frames in Minecraft, so the two result types remain separate below.

## Automated validation

`VisualAssetIntegrityTest` scans all CFR block models and textures during `gradlew build`.

- Every direct `cfrsignals:` model texture reference resolves to a PNG.
- Every PNG decodes, has power-of-two dimensions, and contains no opaque missing-texture magenta pixels.
- Every model element has positive size and remains within Minecraft's `-16..32` coordinate range.
- No CFR block model declares `cullface`, preventing block-neighbor face removal from affecting standalone block-entity components.
- The root semaphore arm and translated extension meet exactly; the round end overlaps the extension.
- The identification plate face is in front of its body, its dynamic text is farther forward, and its bracket reaches the mast.
- Centralized layout tests cover symmetric LEFT/RIGHT offsets, centered CENTER placement, plate scale/positions, text scaling, and the 48-block text cutoff.

The client resource reload must also complete without CFR missing-model or missing-texture messages. A dedicated server launch verifies that visual code remains isolated from the server.

## Manual configuration matrix

Create these assemblies with the CFR wrench:

| Family | State | Alignment | Plate text |
|---|---|---|---|
| 2-Light | RED and GREEN | LEFT, CENTER, RIGHT | `X4` |
| 3-Light | RED, YELLOW, GREEN | LEFT, CENTER, RIGHT | `A` |
| Mechanical | STOP and PROCEED | fixed mounting | `S3` |

For each representative assembly, inspect the front, rear, left, right, below, and slightly above. Repeat in daytime, nighttime, and rain without shaders.

## Acceptance checklist

- The complete assembly remains visible while orbiting the controller block; the tall mast and extended semaphore arm must not vanish at the edge of the camera frustum.
- No surface uses the black/magenta missing-texture pattern.
- Lamps remain horizontally centered and recessed inside their housing.
- Modern head brackets and the mechanical saddle visibly connect to the mast.
- The semaphore arm, extension, round end, pivot, linkage, and counterweight have no visible gaps.
- Plate geometry is visible, attached, correctly oriented, and does not clip into the head or cabinet at HIGH/NORMAL/LOW positions.
- `X4`, `A`, and `S3` are centered and do not flicker against the plate face.
- Only active lenses are emissive; the housing and painted hardware remain normally lit.
- Textures do not visibly stretch, faces do not reverse, and no surface flickers from z-fighting.

## Current test boundary

Build, asset validation, client resource loading, and dedicated-server startup are automated. The view/weather matrix requires a player-controlled in-game camera and must be completed manually before declaring final visual approval.

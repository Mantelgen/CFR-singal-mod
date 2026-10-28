# Visual performance pass

Milestone 28 reviews the renderer for yards containing approximately 50–200 visible CFR signals.

## Existing efficient behavior retained

- A complete signal uses one block entity in its head. Base and pole sections are ordinary static blocks with no tickers.
- Every static component is a baked JSON model. Geometry and textures are not generated or rebuilt per frame.
- Opaque/cutout signal parts share one render path; there are no translucent passes for the mast, housing, arm, ladder, cabinet, or plate.
- Only the selected dark/lit lamp model is rendered for each lens.
- Semaphore interpolation is client-local from synchronized target state. It does not send animation packets each frame.
- Railway state resolution remains server-side and does not scan trains or the surrounding world from the renderer.

## Changes

The entity-compatible cutout render type is now resolved once when the renderer class initializes instead of once for every component model draw.

Identification text is rendered only when the signal controller is within 48 blocks of the camera. The physical plate remains visible at longer distances, but unreadably small font quads are not submitted. The distance calculation uses primitive coordinates and does not construct a new position vector per signal per frame.

The corrected head render bounding box contains side-aligned heads and the complete rotating 2.3-block semaphore arm. Static base and pole models use normal block-model culling.

## Deliberately not changed

Pole sections are independent blocks but deliberately not block entities or runtime meshes. Models were not merged into oversized JSON files, and Flywheel instancing was not introduced solely for this renderer.

## Manual large-yard test

Place rows containing 50, 100, and 200 signals with a mix of both modern families and mechanical semaphores. Enable every attachment and give plates different short labels. Compare frame timing while standing within 32 blocks, near the 48-block text cutoff, and beyond it. Rotate the camera so assemblies enter and leave the frustum, then switch several semaphore aspects and confirm that arm interpolation remains smooth.

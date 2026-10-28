# CFR signal model workflow

The CFR signal renderer assembles several ordinary Minecraft Java block models. Each file is intentionally an independent module that can be opened and edited in Blockbench without needing Blender or a monolithic signal model.

## Coordinate convention

- Models use the standard `16 x 16 x 16` Minecraft block coordinate space.
- The controller's front is model north (`-Z`). The renderer rotates the complete assembly to the block's horizontal facing.
- One mast segment is exactly 16 model units tall. The renderer stacks at least five segments for every signal family, producing an overall default height of roughly six blocks once the head is included.
- Standalone head and attachment models keep their origin at the controller block origin. Do not recenter their pivots during export.
- Geometry may extend outside the visual controller block because the block entity has an expanded render bounding box.

## Editable modules

| Module | Model file | Responsibility |
| --- | --- | --- |
| Foundation | `block/mast/concrete_base.json` | Stepped concrete footing, cap plate, anchor bolts |
| Mast band | `block/mast/segment.json` | Shared post geometry inherited by red and white variants |
| Two-light shell | `block/head/two_light_backplate.json` | Rounded backing plate, rear rails, mast brackets, fasteners |
| Two-light visors | `block/head/two_light_hoods.json` | Deep upper/lower hoods and metal bezel blocks |
| Two-light red lens | `block/head/lamp_red.json` | Small stepped red lens geometry |
| Two-light green lens | `block/head/lamp_green.json` | Small stepped green lens geometry |
| Three-light shell | `block/head/three_light_backplate.json` | Dedicated taller rounded shell with rear casing and mounting spine |
| Three-light visors | `block/head/three_light_hoods.json` | Independent upper, middle, and lower visor geometry |
| Three-light lenses | `block/head/three_light_lamp_*.json` | Separate red, yellow, and green lens positions |
| Side head bracket | `block/head/side_mount_bracket.json` | Bridges LEFT/RIGHT heads to the mast; mirrored in the renderer |
| Ladder | `block/attachments/ladder_segment.json` | Rails, four rungs, mast stand-offs |
| Cabinet | `block/attachments/electrical_box.json` | Cabinet body, raised door, handle, hinges, mounting channel |
| ID plate | `block/attachments/identification_plate.json` | X4 plate and rear mounting arm |

The lit and dark lamp files inherit their color/position geometry and only replace the `lens` texture. Preserve that inheritance when editing the lens shapes. The temporary yellow lens uses vanilla brown/yellow stained-glass textures until a dedicated CFR texture pass.

## Blockbench setup

1. Create a **Java Block/Item** project.
2. Set texture size to `16 x 16`.
3. Import one module JSON with **File → Import → Java Block/Item Model**.
4. Keep north as the visible face of the signal.
5. Edit only that module's responsibility. Use the other files as read-only reference imports when alignment is needed.
6. Export with **File → Export → Export Block/Item Model** over the same module file.
7. Check that texture identifiers retain the `cfrsignals:block/...` namespace and that parent models retain their parent link.

The black housing and all lamp/hood models share the same centered origin. The renderer applies the complete-head LEFT/CENTER/RIGHT transform once through `CFRSignalVisualLayout`; do not bake alignment offsets into individual lamp models.

Every modern lens is centered on model X `8`, independent of complete-head alignment. Lenses use a compact stepped two-model-unit silhouette, sit behind an open four-piece dark bezel, and remain recessed under the deeper hood geometry. Keep lit and dark variants inheriting the same geometry so changing aspect cannot shift a lamp.

## Geometry constraints

- Keep the two-light order red above green and the three-light order red, yellow, green from top to bottom.
- Keep lenses smaller than the backing plate and behind the visor fronts.
- Preserve the stepped top and bottom silhouette; it approximates rounded CFR backing plates with cuboids.
- Avoid rotated elements unless they materially improve the silhouette. Axis-aligned cuboids render consistently through the current baked-model path.
- Do not combine all modules into one exported model. Visibility of the ladder, cabinet, and plate is controlled independently by configuration.
- Do not bake lit and dark lamps into the head shell. The renderer swaps those models according to the synchronized signal aspect.

## Validation

After exporting models:

```powershell
Get-ChildItem src/main/resources/assets/cfrsignals/models -Recurse -Filter *.json |
    ForEach-Object { Get-Content -Raw $_.FullName | ConvertFrom-Json | Out-Null }
\.\gradlew.bat build
\.\gradlew.bat runClient
```

In game, view the signal from the front, side, and rear; test all four horizontal facings; toggle every attachment; switch families with yellow dye; and verify RED, YELLOW, and GREEN illuminated states on the three-light head.

# Component mounting audit

Milestone 26 verifies that every visible module has a physical connection to the mast.

| Component | Connection |
| --- | --- |
| Centered modern head | Four-piece rear saddle bridges the forward housing spine to a mast collar. |
| Left/right modern head | Mirrored side bracket overlaps both the shifted head spine and mast clamp. |
| Ladder | Two standoffs per repeated segment overlap the rear mast face and ladder rails. |
| Electrical cabinet | Vertical channel joins the cabinet, while upper and lower wraparound clamps engage the mast. |
| Identification plate | Rear neck joins the plate body to a broad mast collar. |
| Mechanical housing | Lower saddle and forward gusset overlap both the housing and mast. |
| Mechanical arm/linkage | Arm, linkage, counterweight, and end modules share the fixed mechanism pivot transform. |
| Dwarf head | The scaled shunting housing overlaps the top of the low concrete base instead of floating above it. |
| Cantilever head | The vertical hanger overlaps both the scaled head casing and the two horizontal beam members. |
| Continuous-pole inline head | The controller renders a full mast-and-ladder segment behind its centered housing, joining the real pole blocks above and below. |
| Wall head | The rear bracket overlaps the shifted head spine and terminates at the supporting block face. |
| Wall identification plate | A dedicated rear arm joins the scaled plate bracket to the supporting block face. |

These are static baked-model parts. Regression tests measure every transformed
junction so a later model or scale change cannot silently reopen a gap. No extra
block entities, per-frame geometry, unrestricted offsets, or railway-state
changes were introduced.
